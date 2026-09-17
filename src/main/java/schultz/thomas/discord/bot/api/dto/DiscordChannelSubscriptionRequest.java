package schultz.thomas.discord.bot.api.dto;

import java.util.List;

public record DiscordChannelSubscriptionRequest(
        List<DiscordChannelSelection> channels
) {
}
