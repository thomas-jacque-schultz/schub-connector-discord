package schultz.thomas.discord.bot.business.mapper;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import schultz.thomas.discord.bot.api.dto.DiscordChannelDto;
import schultz.thomas.discord.bot.api.dto.DiscordChannelSelection;
import schultz.thomas.discord.bot.api.dto.DiscordGuildChannelsDto;
import schultz.thomas.discord.bot.data.entity.ChannelEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public abstract class DiscordChannelMapper {

    public DiscordGuildChannelsDto toGuildDto(Guild guild, Set<String> subscribedChannelIds) {
        List<DiscordChannelDto> channels = guild.getTextChannels().stream()
                .map(channel -> toChannelDto(channel, subscribedChannelIds))
                .toList();

        return new DiscordGuildChannelsDto(guild.getId(), guild.getName(), channels);
    }

    public DiscordChannelDto toChannelDto(TextChannel channel, Set<String> subscribedChannelIds) {
        return new DiscordChannelDto(
                channel.getId(),
                channel.getName(),
                subscribedChannelIds.contains(channel.getId())
        );
    }

    public ChannelEntity toChannelEntity(DiscordChannelSelection selection) {
        return toChannelEntity(selection.channelId(), selection.channelName(), selection.guildId());
    }

    public ChannelEntity toChannelEntity(String channelId, String channelName, String guildId) {
        ChannelEntity channel = new ChannelEntity();
        channel.setChannelId(channelId);
        channel.setName(channelName);
        channel.setGuildId(guildId);
        channel.setMessages(new ArrayList<>());
        return channel;
    }
}
