package schultz.thomas.discord.bot.business.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import schultz.thomas.discord.bot.model.view.GameServerView;

import java.util.List;
import java.util.Optional;

/**
 * La vue que le connecteur a du domaine : une copie de travail, jamais une source de vérité.
 *
 * <p>Rafraîchie par le pull périodique et par les poussées du cœur. Un échec de lecture
 * conserve la vue précédente : afficher un état ancien vaut mieux qu'effacer les messages
 * Discord parce que le cœur redémarrait.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameServerViewService {

    private final CoreClient coreClient;

    // Référence volatile vers une liste immuable : le champ n'est jamais muté, il est
    // remplacé en bloc. C'est la publication sûre correcte, et le List.copyOf du refresh
    // garantit l'immuabilité que ce raisonnement suppose — la liste rendue par le cœur
    // est un ArrayList que all() laissait fuir tel quel.
    private volatile List<GameServerView> view = List.of();

    /** Rend vrai si la lecture a abouti. */
    public boolean refresh() {
        try {
            view = List.copyOf(coreClient.fetchAll());
            return true;
        } catch (RuntimeException e) {
            log.warn("Lecture du cœur impossible, la vue précédente est conservée : {}", e.getMessage());
            return false;
        }
    }

    public List<GameServerView> all() {
        return view;
    }

    public Optional<GameServerView> bySlug(String slug) {
        return view.stream().filter(server -> slug.equals(server.getSlug())).findFirst();
    }
}
