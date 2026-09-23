package schultz.thomas.discord.bot.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandEnum {

    SUSBSCRIBE_A_CHANNEL("subscribe"),

    UPDATE_ALL_EXISTING_MESSAGES("refresh"),
    UPDATE_ALL_EXISTING_MESSAGES_SGAMING("refresh-server"),

    START_SGAMING("start"),
    STOP_SGAMING("pause"),

    REFRESH_GAMING_SERVER_MESSAGE("refresh-status");

    private final String commandName;

}
