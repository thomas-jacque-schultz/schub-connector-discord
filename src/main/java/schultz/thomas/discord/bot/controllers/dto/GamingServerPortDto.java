package schultz.thomas.discord.bot.controllers.dto;

/** Port à ouvrir sur la Freebox pendant que le serveur tourne. */
public record GamingServerPortDto(
        /** "tcp" ou "udp" */
        String proto,
        /** port ouvert côté Internet */
        Integer wanPort,
        /** port visé côté LAN ; null = identique à wanPort */
        Integer lanPort,
        /** IP LAN visée ; null = valeur par défaut de la configuration */
        String lanIp
) {
}
