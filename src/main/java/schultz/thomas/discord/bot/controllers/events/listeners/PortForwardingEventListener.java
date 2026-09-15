package schultz.thomas.discord.bot.controllers.events.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import schultz.thomas.discord.bot.business.services.PortForwardingService;
import schultz.thomas.discord.bot.controllers.events.models.GamingServerEvent;

/**
 * Réaligne les redirections du routeur dès qu'un serveur change d'état ou de configuration,
 * sans attendre le passage du scheduler.
 *
 * <p>Listener séparé de {@link GamingServersEventListener} pour éviter une dépendance circulaire :
 * PortForwardingService lit GamingServerService, qui publie ces événements.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortForwardingEventListener {

    private final PortForwardingService portForwardingService;

    @Async
    @EventListener
    public void handleGamingServerEvent(GamingServerEvent event) {
        if (event.getGamingServerEntity() == null) {
            return;
        }
        portForwardingService.reconcile();
    }
}
