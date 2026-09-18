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
 *
 * <p><strong>Ce service ne juge plus des droits, il pose la question au cœur.</strong> Avant le
 * 18-09 il comparait un {@code UserPrivilegeEnum} local à une liste de rôles par commande —
 * pendant que le front appliquait une règle différente sur les mêmes actions. Deux règles pour
 * un même système, c'est une règle de trop : c'est le cœur qui décide, et lui seul sait qu'une
 * personne figure dans les {@code admins} d'un serveur.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommandExecutorService {

    private final CommandSelector commandSelector;

    private final CoreClient coreClient;

    public void handleCommand(SlashCommandInteractionEvent discordContext) {
        // Premier geste, avant toute lecture en base ou appel HTTP : le compte à rebours court déjà.
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
            // Sans ce filet, une commande qui échoue laisse l'interaction en « réfléchit… »
            // pour toujours : l'utilisateur n'apprend jamais que ça s'est mal passé.
            log.error("Échec de la commande '{}' : {}", commandName, e.getMessage(), e);
            respond(discordContext, e.getMessage() != null ? e.getMessage() : "La commande a échoué.");
        }
    }

    /**
     * Le cœur répond, le connecteur applique.
     *
     * <p>Si le cœur est injoignable, on refuse. C'est le seul sens acceptable : exécuter faute
     * de réponse ferait d'une panne du cœur un contournement de l'autorisation, et c'est
     * précisément la porte qu'on vient de fermer.</p>
     */
    private boolean isAllowed(Command command, CommandContext context, String actorId, String actorName) {
        try {
            Set<String> effective = coreClient.effectivePermissions(
                    actorId, actorName, command.scopedServerSlug(context));
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

    /** Répond par le webhook de l'interaction différée, et non par {@code reply()}. */
    private void respond(SlashCommandInteractionEvent discordContext, String message) {
        discordContext.getHook().sendMessage(message).queue();
    }
}
