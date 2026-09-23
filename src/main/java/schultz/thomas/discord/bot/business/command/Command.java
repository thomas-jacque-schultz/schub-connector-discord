package schultz.thomas.discord.bot.business.command;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import schultz.thomas.discord.bot.business.exceptions.CommandFailedException;
import schultz.thomas.discord.bot.data.enums.CommandEnum;
import schultz.thomas.discord.bot.data.enums.PermissionEnum;

public interface Command {

    PermissionEnum permissionNeeded();

    CommandData getCommandData();

    CommandEnum getEnum();

    String execute(CommandContext context) throws CommandFailedException;
}
