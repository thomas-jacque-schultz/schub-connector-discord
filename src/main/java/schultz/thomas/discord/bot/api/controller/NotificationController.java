package schultz.thomas.discord.bot.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import schultz.thomas.discord.bot.business.services.DiscordMessageService;
import schultz.thomas.discord.bot.business.services.GameServerViewService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GameServerViewService gameServerViewService;
    private final DiscordMessageService discordMessageService;
    private final JDA jda;

    @PostMapping("/gameserver-changed")
    public ResponseEntity<Void> gameServerChanged(@RequestBody Map<String, String> body) {
        String slug = body.get("slug");
        log.debug("Changement poussé par le cœur pour '{}'", slug);

        if (gameServerViewService.refresh() && slug != null) {
            gameServerViewService.bySlug(slug)
                    .ifPresent(server -> discordMessageService.createOrUpdateMessageForGamingServerEntity(server, jda));
        }
        return ResponseEntity.accepted().build();
    }
}
