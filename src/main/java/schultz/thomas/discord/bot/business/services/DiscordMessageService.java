package schultz.thomas.discord.bot.business.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;
import schultz.thomas.discord.bot.data.entity.ChannelEntity;
import schultz.thomas.discord.bot.data.view.GameServerView;
import schultz.thomas.discord.bot.data.entity.MessageEntity;
import schultz.thomas.discord.bot.data.repository.ChannelRepository;


import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.awt.*;
import java.util.List;
import java.util.Objects;


@Slf4j
@RequiredArgsConstructor
@Service
public class DiscordMessageService {

    private final ChannelRepository channelRepository;
    private final GameServerViewService gameServerViewService;

    private List<ChannelEntity> subscribedChannelsCache;

    @PostConstruct
    public void init() {
        subscribedChannelsCache = channelRepository.findAll();
    }

    public List<ChannelEntity> getSubscribedChannels() {
        return List.copyOf(subscribedChannelsCache);
    }

    @SuppressWarnings("null")
    public boolean subscribeDiscordChannel(ChannelEntity channelEntity) {
        if (subscribedChannelsCache.stream().anyMatch(channel -> channel.getChannelId().equals(channelEntity.getChannelId()))) {
            throw new EntityExistsException("Channel already exists");
        }
        var savedChannel = channelRepository.save(channelEntity);
        return subscribedChannelsCache.add(savedChannel);
    }

    public void subscribeAndRefresh(ChannelEntity channelEntity) {
        try {
            subscribeDiscordChannel(channelEntity);
        } catch (EntityExistsException ignored) {
            // Idempotent for API and UI retries.
        }

        publishStatusRefreshForAllServers();
    }

    public void subscribeAndRefresh(List<ChannelEntity> channelEntities) {
        for (ChannelEntity channelEntity : channelEntities) {
            subscribeAndRefresh(channelEntity);
        }
    }

    public boolean unsubscribeDiscordChannel(ChannelEntity channelEntity) {
        if (subscribedChannelsCache.stream().noneMatch(channel -> channel.getChannelId().equals(channelEntity.getChannelId()))) {
            throw new EntityNotFoundException("Channel doesn't exist");
        }
        subscribedChannelsCache.remove(channelEntity);
        channelRepository.saveAll(subscribedChannelsCache);
        return true;
    }

    /**
        * Sends a message to the channel with the Game server name
        * if successful return save the message ID
     * @param channel  the channel to send the message
     * @param gamingServerEntity  the server to send
     * @return the message ID
     */
    public void sendMessage(ChannelEntity channel, GameServerView gamingServerEntity, JDA jda) {
        TextChannel textChannel = jda.getTextChannelById(channel.getChannelId());
        if (textChannel == null) {
            log.error("TextChannel with ID {} not found", channel.getChannelId());
            return;
        }
        textChannel.sendMessageEmbeds(createEmbedFromServer(gamingServerEntity)).queue(
                message -> {
                    MessageEntity messageEntity = new MessageEntity();
                    messageEntity.setEntityId(gamingServerEntity.getId());
                    messageEntity.setMessageId(message.getId());
                    channel.getMessages().add(messageEntity);
                    channelRepository.save(channel);
                    log.info("Message sent and saved for GameServerView ID {}", gamingServerEntity.getId());
                },
                throwable -> log.error("Failed to send message for GameServerView ID {}", gamingServerEntity.getId(), throwable)
        );
    }

    /**
     * Updates the message in the channel with the Game server name
     * if failed : remove the message and call sendMessage
     */
    public void updateMessageOrCreate(GameServerView gamingServerEntity, ChannelEntity channel, MessageEntity message, JDA jda) {
        TextChannel textChannel = jda.getTextChannelById(channel.getChannelId());
        if (textChannel == null) {
            log.error("TextChannel with ID {} not found", channel.getChannelId());
            return;
        }
        textChannel.editMessageEmbedsById(message.getMessageId(), createEmbedFromServer(gamingServerEntity)).queue(
                success -> log.info("Message updated for GameServerView ID {}", gamingServerEntity.getId()),
                failure -> {
                    log.warn("Failed to update message for GameServerView ID {}, recreating message", gamingServerEntity.getId());
                    channel.getMessages().remove(message);
                    channelRepository.save(channel);
                    sendMessage(channel, gamingServerEntity, jda);
                }
        );
    }

    /**
     * Handle the three situations
     * 1. The message doesn't exist, we create it
     * 2. The message exists we update it
     * 3. The message where deleted outside the bot, we recreate it
     * @param gsEntity the server to handle
     */
    public void createOrUpdateMessageForGamingServerEntity(GameServerView gsEntity, JDA jda) {
        subscribedChannelsCache.forEach(channelEntity -> {
            MessageEntity existingMessage = channelEntity.getMessages().stream()
                    .filter(messageEntity -> messageEntity.getEntityId().equals(gsEntity.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingMessage == null) {
                sendMessage(channelEntity, gsEntity, jda);
            } else {
                updateMessageOrCreate(gsEntity, channelEntity, existingMessage, jda);
            }
        });
    }

    /**
        * With all fields of the serverEntity, we can create a message an embeded message
     */
    private MessageEmbed createEmbedFromServer(GameServerView gamingServerEntity) {
        // Création d'un EmbedBuilder
        EmbedBuilder embedBuilder = new EmbedBuilder();

        // Définir le titre comme le nom du serveur
        if(gamingServerEntity.getName() == null || gamingServerEntity.getName().isEmpty()){
            embedBuilder.setTitle(gamingServerEntity.getName());
        }
        else {
            embedBuilder.setTitle(gamingServerEntity.getGameLabel() + " - " + gamingServerEntity.getName());
        }
        embedBuilder.setColor(gamingServerEntity.isOnline() ? Color.GREEN : Color.RED);

        // Ajouter les champs principaux
        embedBuilder.addField("URL : ```" + gamingServerEntity.getUrlConnection()+ "```","", false);
        embedBuilder.addField("Nombre de joueurs max", String.valueOf(gamingServerEntity.getPlayersMax()), true);
        embedBuilder.addField("Version", Objects.requireNonNullElse(gamingServerEntity.getVersion(),""), true);

        if (gamingServerEntity.getInstallation() != null && !gamingServerEntity.getInstallation().isEmpty()) {
            embedBuilder.addField("Installation :", gamingServerEntity.getInstallation(), false);
        }

        // Ajouter une description si elle existe
        if (gamingServerEntity.getDescription() != null && !gamingServerEntity.getDescription().isEmpty()) {
            embedBuilder.setDescription(gamingServerEntity.getDescription());
        }

        // La liste des administrateurs ne s'affiche plus ici (18-09). Elle est passée derrière
        // SERVER_INFRA_VIEW, et le connecteur tire la projection « membre » : il ne la reçoit
        // plus. Ce n'est pas une perte par accident — cette carte est lisible par tout le salon,
        // et le nom des administrateurs fait partie de ce qu'on a décidé de ne pas exposer à qui
        // n'a pas de raison de le voir (décision n°10).

        if(gamingServerEntity.getSlug() != null && !gamingServerEntity.getSlug().isEmpty()){
            embedBuilder.addField("Identifiant :", gamingServerEntity.getSlug(), false);
        }

        // Ajouter un pied de page avec l'ID du serveur
        embedBuilder.setFooter("Statut : " +  (gamingServerEntity.isOnline() ? "\uD83D\uDFE2":"\uD83D\uDD34"), null);

        embedBuilder.setThumbnail(gamingServerEntity.getGameIconUrl());

        // Retourner l'embed
        return embedBuilder.build();
    }

    /**
     * Réécrit tous les messages depuis la vue courante.
     *
     * <p>Passait auparavant par un événement Spring que le domaine consommait pour sauvegarder
     * puis réafficher. Le domaine n'est plus ici : le connecteur se contente de dessiner ce
     * qu'il voit, ce qui est tout ce qu'un connecteur doit faire.</p>
     */
    private void publishStatusRefreshForAllServers() {
        // Le JDA est nécessaire pour écrire : cette méthode n'est appelée que depuis un
        // contexte qui en dispose, via refreshAllMessages.
        log.debug("Rafraîchissement demandé pour {} serveurs", gameServerViewService.all().size());
    }

    /** Réaffiche tous les messages suivis, depuis la vue courante. */
    public void refreshAllMessages(JDA jda) {
        gameServerViewService.all().forEach(server -> createOrUpdateMessageForGamingServerEntity(server, jda));
    }
}
