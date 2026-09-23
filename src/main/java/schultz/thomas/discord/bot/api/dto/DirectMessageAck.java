package schultz.thomas.discord.bot.api.dto;

public record DirectMessageAck(boolean delivered, String via) {

    public static DirectMessageAck direct() {
        return new DirectMessageAck(true, "DIRECT_MESSAGE");
    }

    public static DirectMessageAck fallback() {
        return new DirectMessageAck(true, "FALLBACK_CHANNEL");
    }

    public static DirectMessageAck lost() {
        return new DirectMessageAck(false, "NONE");
    }
}
