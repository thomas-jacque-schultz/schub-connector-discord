package schultz.thomas.discord.bot.business.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import schultz.thomas.discord.bot.model.view.GameServerView;

import java.util.List;

/**
 * Le seul lien du connecteur avec le domaine.
 *
 * <p>Depuis la phase 3, ce service ne détient plus rien : ni entité, ni base, ni logique de
 * ports. Il traduit une intention Discord en appel au cœur, et affiche ce que le cœur rapporte.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoreClient {

    @Qualifier("coreRestClient")
    private final RestClient restClient;

    public List<GameServerView> fetchAll() {
        List<GameServerView> servers = restClient.get()
                .uri("/game-servers")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return servers == null ? List.of() : servers;
    }

    /**
     * Demande le démarrage. Le cœur répond 202 : la stack met des dizaines de secondes à
     * répondre, et c'est la boucle d'observation qui constatera le passage à ONLINE.
     */
    public void start(String slug) {
        restClient.post().uri("/game-servers/{slug}/start", slug).retrieve().toBodilessEntity();
    }

    public void stop(String slug) {
        restClient.post().uri("/game-servers/{slug}/stop", slug).retrieve().toBodilessEntity();
    }
}
