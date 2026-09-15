package schultz.thomas.discord.bot.controllers.dto;

import java.time.Instant;
import java.util.List;

public record GamingServerDto(
        String id,
        String identifier,
        Integer portainerStackId,
        String name,
        String urlConnection,
        String gameName,
        Integer playersMax,
        String installation,
        String version,
        String description,
        List<String> admins,
        /** ports pilotés sur la Freebox ; absent d'un PUT = ports inchangés */
        List<GamingServerPortDto> ports,
        /** null = never checked; UNKNOWN/ONLINE/OFFLINE/UNREACHABLE otherwise */
        String status,
        /** ISO-8601 timestamp of the last Portainer check, null if never checked */
        Instant lastStatusCheckAt,
        /** ISO-8601 timestamp of latest status change */
        Instant lastStatusChangeAt,
        /** chronological history of status starts */
        List<GamingServerStatusHistoryEntryDto> statusHistory
) {
}
