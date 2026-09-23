package schultz.thomas.discord.bot.business.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;
import schultz.thomas.discord.bot.business.services.CoreClient;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandExecutorService {

    private final CommandSelector commandSelector;

    private final CoreClient coreClient;

    public void handleCommand(SlashCommandInteractionEvent discordContext) {
        // deferReply en premier : Discord n'accorde que 3 s, puis 10062 Unknown interaction.
        discordContext.deferReply().queue();

        Map<String, String> options = discordContext.getOptions().stream()
                .collect(Collectors.toMap(OptionMapping::getName, OptionMapping::getAsString));
        String actorId = discordContext.getUser().getId();
        String actorName = discordContext.getUser().getName();
        options.put("user-id", actorId);
        options.put("user-name", actorName);
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

        if (!isAllowed(command, context, actorId, actorName)) {
            respond(discordContext, "The emperor of Holy Terra didn't allow you to : " + commandName);
            return;
        }

        try {
            respond(discordContext, command.execute(context));
        } catch (RuntimeException e) {
            log.error("Échec de la commande '{}' : {}", commandName, e.getMessage(), e);
            respond(discordContext, e.getMessage() != null ? e.getMessage() : "La commande a échoué.");
        }
    }

    // Cœur injoignable = refus : une panne du cœur ne doit pas contourner l'autorisation.
    private boolean isAllowed(Command command, CommandContext context, String actorId, String actorName) {
        try {
            Set<String> effective = coreClient.effectivePermissions(actorId, actorName);
            if (effective.contains(command.permissionNeeded().name())) {
                return true;
            }
            log.warn("Commande '{}' refusée à {} : {} manquante",
                    context.getCommandName(), actorId, command.permissionNeeded());
            return false;
        } catch (RuntimeException e) {
            log.error("Autorisation indisponible pour '{}' — commande refusée : {}",
                    context.getCommandName(), e.getMessage());
            return false;
        }
    }

    // Interaction différée : répondre par le hook, pas par reply().
    private void respond(SlashCommandInteractionEvent discordContext, String message) {
        discordContext.getHook().sendMessage(message).queue();
    }
}
