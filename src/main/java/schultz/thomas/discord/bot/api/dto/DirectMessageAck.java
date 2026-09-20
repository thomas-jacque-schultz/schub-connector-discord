package schultz.thomas.discord.bot.api.dto;

/**
 * Ce qu'il est advenu d'un message privé.
 *
 * <p>`delivered` ne veut pas dire « le message privé est parti » mais <strong>« le message n'est
 * pas perdu »</strong>. La nuance est tout l'intérêt de `via` : un message remis par le salon de
 * repli est bien arrivé, mais pas là où on l'attendait — et c'est le signal qu'il faut rouvrir
 * les MP du compte destinataire.</p>
 *
 * @param delivered vrai si le message a été remis, par l'un ou l'autre chemin
 * @param via       {@code DIRECT_MESSAGE}, {@code FALLBACK_CHANNEL} ou {@code NONE}
 */
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
