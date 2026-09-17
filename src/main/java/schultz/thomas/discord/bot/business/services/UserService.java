package schultz.thomas.discord.bot.business.services;

import schultz.thomas.discord.bot.data.entity.UserEntity;
import schultz.thomas.discord.bot.data.enums.UserPrivilegeEnum;

public interface UserService {

    void createUser(UserEntity ue);

    void updateUser(UserEntity ue);

    UserPrivilegeEnum getRole(String discordId);

    UserEntity get(String discordId);
}
