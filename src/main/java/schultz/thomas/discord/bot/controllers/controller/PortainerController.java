package schultz.thomas.discord.bot.controllers.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import schultz.thomas.discord.bot.business.services.ContainerRequestService;
import schultz.thomas.discord.bot.model.transitory.PortainerStack;

import java.util.List;

/**
 * Lecture du catalogue Portainer, pour lier un serveur à sa stack sans la saisir à la main.
 *
 * <p>{@code portainerStackId} est une clé de liaison : une faute de frappe ne se voit qu'au
 * premier démarrage raté, longtemps après la saisie. Proposer la liste supprime la classe
 * d'erreur entière plutôt que de la signaler après coup.</p>
 *
 * <p>Passera dans {@code schub-connector-portainer} en phase 2, d'où le chemin {@code /portainer}
 * qui préfigure son futur {@code GET /stacks}.</p>
 */
@RestController
@RequestMapping("/portainer")
@RequiredArgsConstructor
public class PortainerController {

    @Qualifier("portainerRequestService")
    private final ContainerRequestService containerRequestService;

    @GetMapping("/stacks")
    public List<PortainerStack> listStacks() {
        return containerRequestService.listStacks();
    }
}
