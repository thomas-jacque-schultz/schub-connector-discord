package schultz.thomas.discord.bot.business.parser;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import schultz.thomas.discord.bot.model.entity.GamingServerEntity;
import schultz.thomas.discord.bot.model.entity.GamingServerPort;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface  GameServerParser {

    //ignored fields are not mapped
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastStatusCheckAt", ignore = true)
    @Mapping(target = "lastStatusChangeAt", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    //discord command option with uppercase
    @Mapping(target = "playersMax", source = "players-max")
    @Mapping(target = "urlConnection", source = "url-connection")
    @Mapping(target = "gameName", source = "game-name")
    //string to list of string
    @Mapping(target = "admins", source = "admins", qualifiedByName = "toAdminsList")
    @Mapping(target = "ports", source = "ports", qualifiedByName = "toPortsList")
    GamingServerEntity toServerEntity(Map<String, String> arguments);

    @Named("toAdminsList")
    default List<String> toAdminsList(String admins) {
        if (admins != null && !admins.isEmpty()) {
            return Arrays.stream(admins.split("\\|"))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Parse la liste des ports à piloter sur la Freebox, au format "tcp:25565|udp:8211:8211".
     * Chaque entrée est "proto:portWan" ou "proto:portWan:portLan" ; portLan vaut portWan par défaut.
     * Le port LAN est celui publié sur le nœud Swarm, pas le port interne au conteneur.
     */
    @Named("toPortsList")
    default List<GamingServerPort> toPortsList(String ports) {
        if (ports == null || ports.isBlank()) {
            return null;
        }
        return Arrays.stream(ports.split("\\|"))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .map(GameServerParser::parsePort)
                .collect(Collectors.toList());
    }

    private static GamingServerPort parsePort(String entry) {
        String[] parts = entry.split(":");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("Port mal formé: '" + entry + "' (attendu proto:portWan[:portLan])");
        }
        String proto = parts[0].trim().toLowerCase();
        if (!"tcp".equals(proto) && !"udp".equals(proto)) {
            throw new IllegalArgumentException("Protocole inconnu dans '" + entry + "' (attendu tcp ou udp)");
        }
        int wanPort = Integer.parseInt(parts[1].trim());
        int lanPort = parts.length == 3 ? Integer.parseInt(parts[2].trim()) : wanPort;
        return new GamingServerPort(proto, wanPort, lanPort, null);
    }
}
