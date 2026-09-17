package schultz.thomas.discord.bot.api.dto;

import java.util.List;

public record DiscordGuildChannelsDto(
        String guildId,
        String guildName,
        List<DiscordChannelDto> channels
) {
}
