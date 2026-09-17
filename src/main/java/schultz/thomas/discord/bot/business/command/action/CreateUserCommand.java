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
import schultz.thomas.discord.bot.business.parser.UserParser;
import schultz.thomas.discord.bot.business.services.UserService;
import schultz.thomas.discord.bot.data.enums.CommandEnum;
import schultz.thomas.discord.bot.data.enums.UserPrivilegeEnum;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CreateUserCommand implements Command {

    private final UserService userService;

    private final UserParser userParser;

    public List<UserPrivilegeEnum> roleNeeded(){
        return new ArrayList<>( List.of(UserPrivilegeEnum.ADMINISTRATOR, UserPrivilegeEnum.OWNER));
    }

    @Override
    public CommandData getCommandData() {
        return  new CommandDataImpl("create-user", "mets à jour les autorisations d'un utilisateur")
                .addOptions(new OptionData(OptionType.STRING, "discord-id", "id discord", true))
                .addOptions(new OptionData(OptionType.STRING, "discord-username", "pseudo", true))
                .addOptions(new OptionData(OptionType.STRING, "privilege", "privilège attribué", true));
    }

    @Override
    public CommandEnum getEnum(){
        return CommandEnum.CREATE_USER;
    }

    @Override
    public String execute(CommandContext context) {
        try {
            userService.createUser(userParser.toUserEntity(context.getOptions()));
        } catch (IllegalArgumentException e) {
            throw new CommandFailedException("Impossible de créer l'utilisateur : " + e.getMessage());
        }
        return "Utilisateur créé";
    }


}
