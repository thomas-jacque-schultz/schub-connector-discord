package schultz.thomas.discord.bot.business.parser;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import schultz.thomas.discord.bot.data.entity.UserEntity;
import schultz.thomas.discord.bot.data.enums.UserPrivilegeEnum;

import java.util.Map;

@Mapper(componentModel = "spring", uses = UserPrivilegeEnum.class)
public interface UserParser   {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authUsername", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "discordId", source = "discord-id")
    @Mapping(target = "discordUsername", source = "discord-username")
    UserEntity toUserEntity(Map<String, String> arguments);

}
