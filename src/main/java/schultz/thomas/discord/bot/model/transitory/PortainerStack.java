package schultz.thomas.discord.bot.model.transitory;

/**
 * Une stack telle que Portainer la connaît, réduite à ce qui sert à la choisir.
 *
 * <p>Sert à lier un serveur de jeu à sa stack sans saisie manuelle : {@code portainerStackId}
 * est une clé de liaison, et une faute de frappe dessus ne se voit qu'au premier démarrage
 * raté.</p>
 *
 * @param running état au moment de la lecture ; purement indicatif, il aide à reconnaître la
 *                bonne stack dans une liste, mais ne doit pas être traité comme un état courant
 */
public record PortainerStack(
        Integer id,
        String name,
        Integer endpointId,
        boolean running
) {
}
