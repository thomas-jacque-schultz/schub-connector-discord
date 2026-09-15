package schultz.thomas.discord.bot.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un port qu'un serveur de jeu doit voir ouvert sur la Freebox pendant qu'il tourne.
 * La redirection correspondante est activée à l'allumage du serveur et désactivée à son extinction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamingServerPort {

    /** "tcp" ou "udp". */
    private String proto;

    /** Port ouvert côté Internet. */
    private Integer wanPort;

    /** Port visé côté LAN ; null = identique à wanPort. */
    private Integer lanPort;

    /** IP LAN visée ; null = port-forwarding.default-lan-ip. */
    private String lanIp;
}
