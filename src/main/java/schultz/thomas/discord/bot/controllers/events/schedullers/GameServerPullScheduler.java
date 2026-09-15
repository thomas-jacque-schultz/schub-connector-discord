package schultz.thomas.discord.bot.controllers.events.schedullers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import schultz.thomas.discord.bot.business.services.DiscordMessageService;
import schultz.thomas.discord.bot.business.services.GameServerViewService;

/**
 * Le pull périodique du connecteur.
 *
 * <p>C'est la moitié « correction » de la règle du §5 : le cœur pousse pour la latence, ce pull
 * rattrape tout ce qui s'est perdu. Un push manqué, un redémarrage du connecteur, une coupure
 * réseau — rien ne laisse les messages Discord durablement faux, sans qu'aucune garantie de
 * livraison ne soit nécessaire.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameServerPullScheduler {

    private final GameServerViewService gameServerViewService;
    private final DiscordMessageService discordMessageService;
    /** Résolu à l'usage : voir la note de RefreshGamingServerMessageCommand sur le cycle JDA. */
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
