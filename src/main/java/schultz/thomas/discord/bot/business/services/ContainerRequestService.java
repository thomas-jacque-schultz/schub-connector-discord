package schultz.thomas.discord.bot.business.services;

import org.springframework.stereotype.Service;
import schultz.thomas.discord.bot.model.transitory.DockerContainerState;

@Service
public interface ContainerRequestService {

    boolean startContainer(Integer stackId);
    boolean stopContainer(Integer stackId);
    DockerContainerState getContainerState(Integer stackId);

    /** Toutes les stacks connues de Portainer, pour en choisir une sans la saisir à la main. */
    java.util.List<schultz.thomas.discord.bot.model.transitory.PortainerStack> listStacks();
}
