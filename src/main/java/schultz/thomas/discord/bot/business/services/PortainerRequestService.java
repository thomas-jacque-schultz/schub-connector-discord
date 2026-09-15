package schultz.thomas.discord.bot.business.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import schultz.thomas.discord.bot.model.transitory.DockerContainerState;
import schultz.thomas.discord.bot.model.transitory.PortainerStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service("portainerRequestService")
public class PortainerRequestService implements ContainerRequestService {

    @Qualifier("portainerRestClient")
    private final RestClient restClient;

    private final PortainerErrorLogger portainerErrorLogger;

    @Override
    public boolean startContainer(Integer stackId) {
        try {
            restClient.post()
                    .uri("/api/stacks/{id}/start?endpointId=2", stackId)
                    .retrieve()
                    .body(String.class);
            return true;
        } catch (RestClientException e) {
            portainerErrorLogger.logRestClientException("start-stack", String.valueOf(stackId), e);
            throw e;
        }
    }

    @Override
    public boolean stopContainer(Integer stackId) {
        try {
            restClient.post()
                    .uri("/api/stacks/{id}/stop?endpointId=2", stackId)
                    .retrieve()
                    .body(String.class);
            return true;
        } catch (RestClientException e) {
            portainerErrorLogger.logRestClientException("stop-stack", String.valueOf(stackId), e);
            throw e;
        }
    }

    @Override
    public List<PortainerStack> listStacks() {
        try {
            List<Map<String, Object>> stacks = restClient.get()
                    .uri("/api/stacks")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (stacks == null) {
                return List.of();
            }
            return stacks.stream()
                    .map(this::toStack)
                    .sorted(Comparator.comparing(PortainerStack::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
        } catch (RestClientException e) {
            portainerErrorLogger.logRestClientException("list-stacks", "all", e);
            throw e;
        }
    }

    /** Portainer capitalise ses clés et renvoie les entiers en Number : on normalise ici. */
    private PortainerStack toStack(Map<String, Object> raw) {
        return new PortainerStack(
                asInt(raw.get("Id")),
                raw.get("Name") != null ? String.valueOf(raw.get("Name")) : null,
                asInt(raw.get("EndpointId")),
                Integer.valueOf(1).equals(asInt(raw.get("Status")))
        );
    }

    private Integer asInt(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    @Override
    public DockerContainerState getContainerState(Integer stackId) {
        try {
            Map<String, Object> stack = restClient.get()
                    .uri("/api/stacks/{id}", stackId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            DockerContainerState state = new DockerContainerState();
            if (stack != null) {
                state.setRunning(Integer.valueOf(1).equals(stack.get("Status")));
            }
            return state;
        } catch (RestClientException e) {
            portainerErrorLogger.logRestClientException("state", String.valueOf(stackId), e);
            throw e;
        }
    }

}
