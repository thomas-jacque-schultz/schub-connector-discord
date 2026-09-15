package schultz.thomas.discord.bot.business.command.action;


import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.springframework.stereotype.Component;
import schultz.thomas.discord.bot.business.command.Command;
import schultz.thomas.discord.bot.business.command.CommandContext;
import schultz.thomas.discord.bot.business.exceptions.CommandFailedException;
import schultz.thomas.discord.bot.business.parser.GameServerParser;
import schultz.thomas.discord.bot.business.services.GamingServerService;
import schultz.thomas.discord.bot.business.services.UserService;
import schultz.thomas.discord.bot.model.entity.GamingServerEntity;
import schultz.thomas.discord.bot.model.enums.CommandEnum;
import schultz.thomas.discord.bot.model.enums.UserPrivilegeEnum;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CreateGamingServerCommand implements Command {

    private final UserService userService;

    private final GamingServerService gamingServerService;

    private final GameServerParser gameServerParser;

    public List<UserPrivilegeEnum> roleNeeded(){
        return new ArrayList<>( List.of(UserPrivilegeEnum.OWNER));
    }

    @Override
    public CommandData getCommandData() {
        return  new CommandDataImpl(CommandEnum.CREATE_GAMING_SERVER.getCommandName(), "mets à jour les autorisations d'un utilisateur")
                .addOptions(new OptionData(OptionType.STRING, "identifier", "identifiant du serveur", true))
                .addOptions(new OptionData(OptionType.STRING, "name", "nom du serveur", true))
                .addOptions(new OptionData(OptionType.STRING, "url-connection", "url de connection", true))
                .addOptions(new OptionData(OptionType.INTEGER, "players-max", "nombre de joueurs max", true))
                .addOptions(new OptionData(OptionType.STRING, "installation", "installation", true))
                .addOptions(new OptionData(OptionType.STRING, "admins", "liste des admins", true))
                .addOptions(new OptionData(OptionType.STRING, "version", "version", false))
                .addOptions(new OptionData(OptionType.STRING, "description", "description", false))
                .addOptions(new OptionData(OptionType.STRING, "game-name", "nom du jeu", false))
                .addOptions(new OptionData(OptionType.STRING, "ports", "ports Freebox, ex: tcp:25565|udp:8211", false));
    }

    @Override
    public CommandEnum getEnum(){
        return CommandEnum.CREATE_GAMING_SERVER;
    }

    @Override
    public String execute(CommandContext context) {
        GamingServerEntity gsEntity;
        try {
            gsEntity = gameServerParser.toServerEntity(context.getOptions());
        } catch (IllegalArgumentException e) {
            // porte le détail du champ fautif (ports mal formés notamment)
            throw new CommandFailedException(e.getMessage());
        }

        try {
            gamingServerService.createGamingServer(gsEntity);
        }
        catch (IllegalArgumentException e) {
            throw new CommandFailedException("Le serveur existe déjà");
        }
        return "Le serveur à été crée";
    }


}
