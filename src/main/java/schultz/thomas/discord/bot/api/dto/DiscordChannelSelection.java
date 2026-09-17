package schultz.thomas.discord.bot.api.dto;

public record DiscordChannelSelection(
        String guildId,
        String channelId,
        String channelName
) {
}
