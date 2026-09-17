package schultz.thomas.discord.bot.api.controller;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import schultz.thomas.discord.bot.business.command.Command;
import schultz.thomas.discord.bot.business.command.CommandContext;
import schultz.thomas.discord.bot.business.command.CommandSelector;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gaming-server/command")
@RequiredArgsConstructor
public class GamingServerCommandController {

    private final CommandSelector commandSelector;
    private final JDA jda;

    @PostMapping("/{commandName}")
    public ResponseEntity<?> executeCommand(@PathVariable String commandName, @RequestBody String serverIdentifier) {
        Map<String, String> options = new HashMap<>();
        options.put("identifier", serverIdentifier);
        options.put("gaming-serveur-identifiant", serverIdentifier);

        CommandContext context = new CommandContext(
                jda,
                commandName,
                options
        );

        Command command = commandSelector.getCommand(commandName);
        if (command == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown command"));
        }

        String message = command.execute(context);

        return ResponseEntity.ok(Map.of(
                "status", HttpResponseStatus.OK.toString(),
                "message", message
        ));
    }
}
