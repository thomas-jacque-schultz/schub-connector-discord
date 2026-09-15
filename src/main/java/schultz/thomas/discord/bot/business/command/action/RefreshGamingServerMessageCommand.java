package schultz.thomas.discord.bot.business.command.action;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.ObjectProvider;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.springframework.stereotype.Component;
import schultz.thomas.discord.bot.business.command.Command;
import schultz.thomas.discord.bot.business.command.CommandContext;
import schultz.thomas.discord.bot.business.exceptions.CommandFailedException;
import schultz.thomas.discord.bot.business.services.DiscordMessageService;
import schultz.thomas.discord.bot.business.services.GameServerViewService;
import schultz.thomas.discord.bot.model.enums.CommandEnum;
import schultz.thomas.discord.bot.model.enums.UserPrivilegeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Retire la vue du cœur et réaffiche le message.
 *
 * <p>Ne sonde plus rien : l'observation appartient au cœur. Cette commande ne fait que forcer
 * le pull que le connecteur aurait fait de lui-même à la minute suivante — utile quand on veut
 * voir tout de suite, sans attendre.</p>
 */
@RequiredArgsConstructor
@Component
public class RefreshGamingServerMessageCommand implements Command {

    private final GameServerViewService gameServerViewService;
    private final DiscordMessageService discordMessageService;
    /**
     * Résolu à l'usage et non à la construction : JDA dépend des écouteurs, qui dépendent du
     * sélecteur, qui dépend des commandes. L'injecter directement ici refermerait le cycle et
     * empêcherait le contexte Spring de démarrer.
     */
    private final ObjectProvider<JDA> jdaProvider;

    public List<UserPrivilegeEnum> roleNeeded() {
        return new ArrayList<>(List.of(UserPrivilegeEnum.ADMINISTRATOR, UserPrivilegeEnum.OWNER));
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("refresh-status", "met à jour le message d'un serveur")
                .addOptions(new OptionData(OptionType.STRING, "gaming-serveur-identifiant", "identifiant du serveur", true));
    }

    @Override
    public CommandEnum getEnum() {
        return CommandEnum.REFRESH_GAMING_SERVER_MESSAGE;
    }

    @Override
    public String execute(CommandContext context) {
        String slug = context.getOptions().get("gaming-serveur-identifiant");

        if (!gameServerViewService.refresh()) {
            throw new CommandFailedException("Le cœur est injoignable, impossible de rafraîchir");
        }

        return gameServerViewService.bySlug(slug)
                .map(server -> {
                    discordMessageService.createOrUpdateMessageForGamingServerEntity(server, jdaProvider.getObject());
                    return "Message mis à jour";
                })
                .orElseThrow(() -> new CommandFailedException("Aucun serveur nommé « " + slug + " »"));
    }
}
