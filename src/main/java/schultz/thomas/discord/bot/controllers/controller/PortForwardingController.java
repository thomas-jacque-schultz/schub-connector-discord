package schultz.thomas.discord.bot.controllers.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import schultz.thomas.discord.bot.business.services.PortForwardingService;
import schultz.thomas.discord.bot.business.services.RedirectionRequestService;
import schultz.thomas.discord.bot.config.PortForwardingProperties;
import schultz.thomas.discord.bot.model.portforwarding.PortForwardingReport;
import schultz.thomas.discord.bot.model.portforwarding.PortRule;

import java.util.List;
import java.util.Map;

/**
 * Inspection et resynchronisation manuelle des redirections de ports.
 * Protégé comme le reste de l'API par le filtre de secret interne.
 *
 * <p>Ne rapporte que ce que ce service décide : la politique, et le motif d'indisponibilité tel
 * que l'interface le rend. L'état du routeur lui-même (adresse, appairage, marqueur) appartient
 * au connecteur et se lit sur son {@code GET /router/status} — le répéter ici ferait resurgir
 * la connaissance de la marque du routeur que la phase 1 vient d'en retirer.</p>
 */
@RestController
@RequestMapping("/port-forwarding")
@RequiredArgsConstructor
public class PortForwardingController {

    private final PortForwardingService portForwardingService;

    private final RedirectionRequestService redirectionRequestService;

    private final PortForwardingProperties portForwardingProperties;

    /** État courant tel que le routeur le rapporte, redirections manuelles comprises. */
    @GetMapping("/rules")
    public ResponseEntity<List<PortRule>> listRules() {
        if (!portForwardingProperties.isEnabled()) {
            return ResponseEntity.status(503).build();
        }
        return ResponseEntity.ok(redirectionRequestService.listRules());
    }

    /** Force une réconciliation immédiate et renvoie le détail de ce qui a été fait. */
    @PostMapping("/reconcile")
    public PortForwardingReport reconcile() {
        return portForwardingService.reconcile();
    }

    /** État de l'intégration, pour diagnostiquer sans lire les logs. */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "enabled", portForwardingProperties.isEnabled(),
                "dryRun", portForwardingProperties.isDryRun(),
                "unavailableReason", String.valueOf(redirectionRequestService.unavailableReason()),
                "defaultLanIp", portForwardingProperties.getDefaultLanIp(),
                "pruneOrphans", portForwardingProperties.isPruneOrphans(),
                "staticRules", portForwardingProperties.getStaticRules().size()
        );
    }
}
