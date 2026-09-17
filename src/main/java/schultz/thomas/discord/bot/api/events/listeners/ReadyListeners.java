package schultz.thomas.discord.bot.api.events.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReadyListeners extends ListenerAdapter {

    private final List<CommandData> getCommandMap;

    @Override
    public void onReady(ReadyEvent event) {
        log.info("The Bot has started");

        event.getJDA().getGuilds().forEach(guild ->
            guild.updateCommands()
                .addCommands(getCommandMap)
                .queue(
                    success -> log.info("Commands synced for guild {}", guild.getName()),
                    error -> log.error("Failed to sync commands for guild {}", guild.getName(), error)
                )
        );

        log.info("The Bot has finished creating discord commands");
    }
}
