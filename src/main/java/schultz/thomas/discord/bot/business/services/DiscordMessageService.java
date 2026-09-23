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

    private MessageEmbed createEmbedFromServer(GameServerView gamingServerEntity) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if(gamingServerEntity.getName() == null || gamingServerEntity.getName().isEmpty()){
            embedBuilder.setTitle(gamingServerEntity.getName());
        }
        else {
            embedBuilder.setTitle(gamingServerEntity.getGameLabel() + " - " + gamingServerEntity.getName());
        }
        embedBuilder.setColor(gamingServerEntity.isOnline() ? Color.GREEN : Color.RED);

        embedBuilder.addField("URL : ```" + gamingServerEntity.getUrlConnection()+ "```","", false);
        embedBuilder.addField("Nombre de joueurs max", String.valueOf(gamingServerEntity.getPlayersMax()), true);
        embedBuilder.addField("Version", Objects.requireNonNullElse(gamingServerEntity.getVersion(),""), true);

        if (gamingServerEntity.getInstallation() != null && !gamingServerEntity.getInstallation().isEmpty()) {
            embedBuilder.addField("Installation :", gamingServerEntity.getInstallation(), false);
        }

        if (gamingServerEntity.getDescription() != null && !gamingServerEntity.getDescription().isEmpty()) {
            embedBuilder.setDescription(gamingServerEntity.getDescription());
        }

        if(gamingServerEntity.getSlug() != null && !gamingServerEntity.getSlug().isEmpty()){
            embedBuilder.addField("Identifiant :", gamingServerEntity.getSlug(), false);
        }

        embedBuilder.setFooter("Statut : " +  (gamingServerEntity.isOnline() ? "\uD83D\uDFE2":"\uD83D\uDD34"), null);

        embedBuilder.setThumbnail(gamingServerEntity.getGameIconUrl());

        return embedBuilder.build();
    }

    private void publishStatusRefreshForAllServers() {
        log.debug("Rafraîchissement demandé pour {} serveurs", gameServerViewService.all().size());
    }

    public void refreshAllMessages(JDA jda) {
        gameServerViewService.all().forEach(server -> createOrUpdateMessageForGamingServerEntity(server, jda));
    }
}
