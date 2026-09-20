package schultz.thomas.discord.bot.api.controller;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import schultz.thomas.discord.bot.business.mapper.DiscordChannelMapper;
import schultz.thomas.discord.bot.business.services.DirectMessageService;
import schultz.thomas.discord.bot.business.services.DiscordMessageService;
import schultz.thomas.discord.bot.api.dto.DirectMessageAck;
import schultz.thomas.discord.bot.api.dto.DirectMessageRequest;
import schultz.thomas.discord.bot.api.dto.DiscordChannelSubscriptionRequest;
import schultz.thomas.discord.bot.api.dto.DiscordGuildChannelsDto;
import schultz.thomas.discord.bot.data.entity.ChannelEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/discord")
@RequiredArgsConstructor
public class DiscordController {

    private final JDA jda;
    private final DiscordMessageService discordMessageService;
    private final DiscordChannelMapper discordChannelMapper;
    private final DirectMessageService directMessageService;

    @GetMapping("/guilds/channels")
    public ResponseEntity<List<DiscordGuildChannelsDto>> getGuildsWithChannels() {
        Set<String> subscribedChannelIds = discordMessageService.getSubscribedChannels().stream()
                .map(ChannelEntity::getChannelId)
                .collect(Collectors.toSet());

        List<DiscordGuildChannelsDto> payload = jda.getGuilds().stream()
            .map(guild -> discordChannelMapper.toGuildDto(guild, subscribedChannelIds))
                .toList();

        return ResponseEntity.ok(payload);
    }

    /**
     * Choisit les salons qui reçoivent les notifications de serveurs.
     *
     * <p>Cette route vivait sur {@code /gaming-server/subscribe-channels} et a disparu avec le
     * contrôleur du domaine à la phase 3 — laissant un bouton du front en 404. Elle est
     * rétablie ici, sous {@code /discord} : choisir un salon est une affaire de Discord, pas de
     * serveur de jeu. Le connecteur n'a d'ailleurs jamais cessé d'en être capable
     * ({@code ChannelRepository}, {@code SubscribeChannelCommand}) ; seul le point d'entrée
     * HTTP manquait.</p>
     */
    @PostMapping("/channels/subscribe")
    public ResponseEntity<Void> subscribeChannels(@RequestBody DiscordChannelSubscriptionRequest request) {
        if (request == null || request.channels() == null || request.channels().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<ChannelEntity> channels = request.channels().stream()
                .map(discordChannelMapper::toChannelEntity)
                .toList();

        discordMessageService.subscribeAndRefresh(channels);
        return ResponseEntity.noContent().build();
    }

    /**
     * Écrit à une personne, en message privé.
     *
     * <p>Le connecteur savait écrire dans des salons, pas à quelqu'un. C'est la capacité
     * ajoutée par le chantier C, et le formulaire de contact du portfolio en est le premier
     * appelant — mais la route ne le sait pas : on lui donne un destinataire, un titre et un
     * corps. Un connecteur qui saurait ce qu'est un formulaire de contact aurait dérivé.</p>
     *
     * <p>Le code de retour distingue trois issues, parce qu'elles appellent trois réactions
     * différentes : <b>200</b>, remis (le corps dit par quel chemin, et un repli mérite qu'on
     * aille rouvrir les MP du compte) ; <b>400</b>, demande inexploitable ; <b>502</b>, ni le
     * message privé ni le repli n'ont abouti — là, le message est perdu, et l'appelant doit le
     * dire franchement à qui l'a écrit.</p>
     *
     * <p>Route interne : elle passe par le secret partagé comme toutes les autres. C'est le BFF
     * qui porte l'exposition publique, et avec elle l'anti-spam.</p>
     */
    @PostMapping("/direct-messages")
    public ResponseEntity<DirectMessageAck> sendDirectMessage(@RequestBody DirectMessageRequest request) {
        if (request == null
                || request.recipientId() == null || request.recipientId().isBlank()
                || request.body() == null || request.body().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        DirectMessageAck ack = directMessageService.send(request);
        return ack.delivered()
                ? ResponseEntity.ok(ack)
                : ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ack);
    }
}
