package schultz.thomas.discord.bot.business.command.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.springframework.stereotype.Component;
import schultz.thomas.discord.bot.business.command.Command;
import schultz.thomas.discord.bot.business.command.CommandContext;
import schultz.thomas.discord.bot.business.exceptions.CommandFailedException;
import schultz.thomas.discord.bot.business.services.CoreClient;
import schultz.thomas.discord.bot.business.services.GameServerViewService;
import schultz.thomas.discord.bot.data.enums.CommandEnum;
import schultz.thomas.discord.bot.data.enums.PermissionEnum;


/**
 * Demande au cœur de démarrer un serveur.
 *
 * <p>Le connecteur ne démarre rien lui-même et ne connaît ni la stack ni les ports : il traduit
 * une intention Discord en appel au cœur, entièrement, et s'arrête là (plan §4).</p>
 *
 * <p>L'option garde le nom {@code identifier}, connu des utilisateurs. Le §2 renomme le code,
 * pas l'ergonomie d'une commande déjà dans les habitudes.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartServerGamingCommand implements Command {

    private final CoreClient coreClient;
    private final GameServerViewService gameServerViewService;

    @Override
    public PermissionEnum permissionNeeded() {
        return PermissionEnum.SERVER_START;
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("start", "lance le serveur de jeu")
                .addOptions(new OptionData(OptionType.STRING, "identifier", "identifiant du serveur", true));
    }

    @Override
    public CommandEnum getEnum() {
        return CommandEnum.START_SGAMING;
    }

    @Override
    public String execute(CommandContext context) {
        String slug = context.getOptions().get("identifier");
        gameServerViewService.bySlug(slug)
                .orElseThrow(() -> new CommandFailedException("Le serveur de jeu n'existe pas"));
        try {
            coreClient.start(slug, context.getOptions().get("user-id"));
        } catch (RuntimeException e) {
            log.warn("Démarrage refusé par le cœur pour '{}' : {}", slug, e.getMessage());
            throw new CommandFailedException("Impossible de lancer le serveur de jeu");
        }
        // Volontairement pas de « serveur lancé » : le cœur a seulement ACCEPTÉ la demande, la
        // stack met des dizaines de secondes à basculer, et c'est la boucle d'observation qui
        // constatera le démarrage. Ne rien promettre non plus sur ce message-ci : c'est la carte
        // d'état du salon abonné qui se rafraîchit, pas cette réponse-là.
        return "Commande reçue et transmise.";
    }
}
