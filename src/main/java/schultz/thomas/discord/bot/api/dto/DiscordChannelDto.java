package schultz.thomas.discord.bot.api.dto;

public record DiscordChannelDto(
        String id,
        String name,
        boolean subscribed
) {
}
