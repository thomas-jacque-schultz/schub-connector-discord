package schultz.thomas.discord.bot.business.command;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import schultz.thomas.discord.bot.business.exceptions.CommandFailedException;
import schultz.thomas.discord.bot.data.enums.CommandEnum;
import schultz.thomas.discord.bot.data.enums.PermissionEnum;

/**
 * Une commande slash.
 *
 * <p><strong>Une commande ne juge plus des droits, elle déclare ce qu'elle exige.</strong>
 * {@code roleNeeded()} et {@code hasRight()} ont disparu le 18-09 : le connecteur ne détenait
 * plus l'identité, et surtout Discord et le front n'appliquaient pas la même règle. C'est
 * exactement ce qu'on supprime — une seule règle, un seul endroit, le cœur (plan §A.2).</p>
 */
public interface Command {

    /** La permission que le cœur devra reconnaître à l'auteur pour que la commande s'exécute. */
    PermissionEnum permissionNeeded();

    /**
     * interaction configuration in discord client
     * @return
     */
    CommandData getCommandData();

    /**
     * Renvoie l'enume de la commande.
     * @return
     */
    CommandEnum getEnum();

    /**
     * Execute la commande.
     * @param context
     * @return le message de retour
     */
    String execute(CommandContext context) throws CommandFailedException;
}
