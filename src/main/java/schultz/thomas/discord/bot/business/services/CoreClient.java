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

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreClient {

    private static final String ACTOR_HEADER = "X-Actor-Id";

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
