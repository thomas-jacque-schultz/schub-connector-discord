package schultz.thomas.discord.bot.api.dto;

public record DirectMessageRequest(String recipientId, String title, String body, String footer) {
}
