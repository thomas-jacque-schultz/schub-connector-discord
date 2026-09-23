package schultz.thomas.discord.bot.api.events.schedullers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import schultz.thomas.discord.bot.business.services.DiscordMessageService;
import schultz.thomas.discord.bot.business.services.GameServerViewService;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameServerPullScheduler {

    private final GameServerViewService gameServerViewService;
    private final DiscordMessageService discordMessageService;
    // ObjectProvider : injecter JDA directement ferme un cycle de beans (JDA → écouteurs → commandes).
    private final ObjectProvider<JDA> jdaProvider;

    public void pull() {
        if (!gameServerViewService.refresh()) {
            return;
        }
        try {
            discordMessageService.refreshAllMessages(jdaProvider.getObject());
        } catch (RuntimeException e) {
            log.warn("Rafraîchissement des messages Discord en échec : {}", e.getMessage());
        }
    }
}
