package schultz.thomas.discord.bot.business.command.action;


import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.springframework.stereotype.Component;
import schultz.thomas.discord.bot.business.command.Command;
import schultz.thomas.discord.bot.business.command.CommandContext;
import schultz.thomas.discord.bot.business.mapper.DiscordChannelMapper;
import schultz.thomas.discord.bot.business.exceptions.CommandFailedException;
import schultz.thomas.discord.bot.business.services.DiscordMessageService;
import schultz.thomas.discord.bot.data.entity.ChannelEntity;
import schultz.thomas.discord.bot.data.enums.CommandEnum;
import schultz.thomas.discord.bot.data.enums.UserPrivilegeEnum;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class SubscribeChannelCommand implements Command {

    private final DiscordMessageService discordMessageService;
    private final DiscordChannelMapper discordChannelMapper;


    public List<UserPrivilegeEnum> roleNeeded(){
        return new ArrayList<>( List.of(UserPrivilegeEnum.OWNER));
    }

    @Override
    public CommandData getCommandData() {
        return  new CommandDataImpl(CommandEnum.SUSBSCRIBE_A_CHANNEL.getCommandName(), "affiche l'état des serveurs dans ce channel");
    }

    @Override
    public CommandEnum getEnum(){
        return CommandEnum.SUSBSCRIBE_A_CHANNEL;
    }

    @Override
    public String execute(CommandContext context) {
        ChannelEntity channel = discordChannelMapper.toChannelEntity(
                context.getOptions().get("channel-id"),
                context.getOptions().get("channel-name"),
                context.getOptions().get("guild-id")
        );

        try {
            discordMessageService.subscribeAndRefresh(channel);
        } catch (RuntimeException e) {
            throw new CommandFailedException("Impossible de créer le channel : " + e.getMessage());
        }
        return "J'utilise maintenant ce channel pour les updates";
    }


}
