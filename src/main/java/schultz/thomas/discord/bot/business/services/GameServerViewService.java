package schultz.thomas.discord.bot.business.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import schultz.thomas.discord.bot.model.view.GameServerView;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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

    /**
     * La vue n'est jamais mutée : elle est remplacée en bloc par une liste immuable.
     * L'{@link AtomicReference} dit cela explicitement, là où un champ {@code volatile}
     * laissait croire qu'on protégeait la liste elle-même. Le {@code List.copyOf} compte
     * autant que l'échange atomique : le cœur rend un {@code ArrayList} que {@link #all()}
     * laissait sinon fuir tel quel aux appelants.
     */
    private final AtomicReference<List<GameServerView>> view = new AtomicReference<>(List.of());

    /** Rend vrai si la lecture a abouti. */
    public boolean refresh() {
        try {
            view.set(List.copyOf(coreClient.fetchAll()));
            return true;
        } catch (RuntimeException e) {
            log.warn("Lecture du cœur impossible, la vue précédente est conservée : {}", e.getMessage());
            return false;
        }
    }

    public List<GameServerView> all() {
        return view.get();
    }

    public Optional<GameServerView> bySlug(String slug) {
        return view.get().stream().filter(server -> slug.equals(server.getSlug())).findFirst();
    }
}
