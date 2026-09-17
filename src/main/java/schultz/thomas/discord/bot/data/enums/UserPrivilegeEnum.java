package schultz.thomas.discord.bot.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserPrivilegeEnum {

        USER("user"),
        MODERATOR("modo"),
        ADMINISTRATOR("admin"),
        OWNER("moi");

        private final String privilegeName;
}

