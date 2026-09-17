package schultz.thomas.discord.bot.api.controller;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import schultz.thomas.discord.bot.business.mapper.DiscordChannelMapper;
import schultz.thomas.discord.bot.business.services.DiscordMessageService;
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
}
