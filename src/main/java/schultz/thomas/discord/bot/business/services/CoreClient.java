package schultz.thomas.discord.bot.business.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import schultz.thomas.discord.bot.data.view.GameServerView;

import java.util.List;
import java.util.Set;

/**
 * Le seul lien du connecteur avec le domaine.
 *
 * <p>Depuis la phase 3, ce service ne détient plus rien : ni entité, ni base, ni logique de
 * ports. Il traduit une intention Discord en appel au cœur, et affiche ce que le cœur rapporte.
 * Depuis le 18-09 il ne détient plus non plus les droits : il les <em>demande</em>.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoreClient {

    private static final String ACTOR_HEADER = "X-Actor-Id";

    @Qualifier("coreRestClient")
    private final RestClient restClient;

    /**
     * Le pull périodique, sans acteur : personne n'est derrière cette requête.
     *
     * <p>Le cœur sert alors la projection « membre » — pas l'infrastructure. Les cartes d'état
     * d'un salon Discord n'ont pas à porter la liste des ports ouverts sur la box.</p>
     */
    public List<GameServerView> fetchAll() {
        List<GameServerView> servers = restClient.get()
                .uri("/game-servers")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return servers == null ? List.of() : servers;
    }

    /**
     * « Que peut faire cette personne ? » — la question posée au cœur avant chaque commande.
     *
     * <p>Le compte est créé au rôle {@code VISITEUR} s'il est inconnu : quelqu'un qui tape une
     * commande dans Discord est quelqu'un du système, même s'il ne s'est jamais connecté au site.</p>
     */
    public Set<String> effectivePermissions(String discordId, String discordUsername) {
        Set<String> permissions = restClient.get()
                .uri(uri -> permissionsUri(uri, discordId, discordUsername))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return permissions == null ? Set.of() : permissions;
    }

    private java.net.URI permissionsUri(UriBuilder uri, String discordId, String discordUsername) {
        uri.path("/users/by-discord/{discordId}/permissions");
        if (discordUsername != null && !discordUsername.isBlank()) {
            uri.queryParam("discordUsername", discordUsername);
        }
        return uri.build(discordId);
    }

    /**
     * Demande le démarrage. Le cœur répond 202 : la stack met des dizaines de secondes à
     * répondre, et c'est la boucle d'observation qui constatera le passage à ONLINE.
     *
     * <p>L'acteur est transmis, et le cœur revérifie. Le contrôle fait ici est une politesse —
     * refuser tôt pour donner un message utile dans Discord ; celui du cœur est la règle.</p>
     */
    public void start(String slug, String actorDiscordId) {
        restClient.post()
                .uri("/game-servers/{slug}/start", slug)
                .headers(headers -> withActor(headers, actorDiscordId))
                .retrieve()
                .toBodilessEntity();
    }

    public void stop(String slug, String actorDiscordId) {
        restClient.post()
                .uri("/game-servers/{slug}/stop", slug)
                .headers(headers -> withActor(headers, actorDiscordId))
                .retrieve()
                .toBodilessEntity();
    }

    private void withActor(org.springframework.http.HttpHeaders headers, String actorDiscordId) {
        if (actorDiscordId != null && !actorDiscordId.isBlank()) {
            headers.set(ACTOR_HEADER, actorDiscordId);
        }
    }

}
