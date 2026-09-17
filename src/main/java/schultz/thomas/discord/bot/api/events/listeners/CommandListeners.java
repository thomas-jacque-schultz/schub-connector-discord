package schultz.thomas.discord.bot.api.events.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import schultz.thomas.discord.bot.business.command.CommandExecutorService;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommandListeners extends ListenerAdapter  {


    private final CommandExecutorService commandExecutorService;

    /**
     * Listener for commands in discord
     * only trigger when a user send a command
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) throws RuntimeException {
        if (filterMessage(event)) return;
        commandExecutorService.handleCommand(event);
    }

    public boolean filterMessage(SlashCommandInteractionEvent event){
        return event.getUser().isBot() ;
    }
}
