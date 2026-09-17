package schultz.thomas.discord.bot.api.dto;

public record AuthLoginResponseDto(String accessToken, String tokenType, long expiresInSeconds) {
}
