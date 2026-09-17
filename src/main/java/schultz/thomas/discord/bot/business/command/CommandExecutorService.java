package schultz.thomas.discord.bot.business.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;
import schultz.thomas.discord.bot.business.services.UserService;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exécute une commande slash et répond à l'utilisateur.
 *
 * <p><strong>Discord donne 3 secondes pour accuser réception d'une interaction</strong>, faute
 * de quoi le jeton expire et l'API répond {@code 10062: Unknown interaction} — l'utilisateur,
 * lui, voit « L'application ne répond plus ». Depuis la phase 3, une commande comme
 * {@code /pause} traverse ce connecteur, puis le cœur, puis le connecteur Portainer, puis
 * l'API de Portainer : les 3 secondes sont dépassées de loin.</p>
 *
 * <p>D'où {@code deferReply()} en tout premier geste, avant le moindre appel : il accuse
 * réception immédiatement et transforme l'interaction en « réfléchit… ». La vraie réponse part
 * ensuite par le webhook, sans limite de temps. C'est la réponse prévue au §5 du plan ;
 * l'architecture n'a pas à s'en mêler.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommandExecutorService {

    private final CommandSelector commandSelector;

    private final UserService userService;

    public void handleCommand(SlashCommandInteractionEvent discordContext) {
        // Premier geste, avant toute lecture en base ou appel HTTP : le compte à rebours court déjà.
        discordContext.deferReply().queue();

        Map<String, String> options = discordContext.getOptions().stream()
                .collect(Collectors.toMap(OptionMapping::getName, OptionMapping::getAsString));
        options.put("user-id", discordContext.getUser().getId());
        options.put("user-name", discordContext.getUser().getName());
        options.put("channel-id", discordContext.getChannel().getId());
        options.put("channel-name", discordContext.getChannel().getName());
        options.put("guild-id", discordContext.getGuild().getId());
        options.put("guild-name", discordContext.getGuild().getName());

        String commandName = discordContext.getName();
        JDA jda = discordContext.getJDA();

        CommandContext context = new CommandContext(jda, commandName, options);

        Command command = commandSelector.getCommand(commandName);

        if (command == null) {
            respond(discordContext, "RTFM : " + commandName);
            log.warn("Commande inconnue : {}", commandName);
            return;
        }

        if (!command.hasRight(userService.getRole(discordContext.getUser().getId()))) {
            respond(discordContext, "The emperor of Holy Terra didn't allow you to : " + commandName);
            log.warn("Commande '{}' refusée à l'utilisateur {}", commandName, discordContext.getUser().getId());
            return;
        }

        try {
            respond(discordContext, command.execute(context));
        } catch (RuntimeException e) {
            // Sans ce filet, une commande qui échoue laisse l'interaction en « réfléchit… »
            // pour toujours : l'utilisateur n'apprend jamais que ça s'est mal passé.
            log.error("Échec de la commande '{}' : {}", commandName, e.getMessage(), e);
            respond(discordContext, e.getMessage() != null ? e.getMessage() : "La commande a échoué.");
        }
    }

    /** Répond par le webhook de l'interaction différée, et non par {@code reply()}. */
    private void respond(SlashCommandInteractionEvent discordContext, String message) {
        discordContext.getHook().sendMessage(message).queue();
    }
}
