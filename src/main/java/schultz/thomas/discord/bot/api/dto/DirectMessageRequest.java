package schultz.thomas.discord.bot.api.dto;

/**
 * Une demande d'envoi en message privé.
 *
 * <p>Le connecteur ne sait pas <em>pourquoi</em> on lui demande d'écrire — c'est la règle du §4
 * de la migration, et elle tient ici : aucun champ ne parle de formulaire de contact, d'adresse
 * e-mail ni de visiteur. On lui donne un destinataire, un titre et un corps ; le sens de ces
 * trois choses appartient à l'appelant.</p>
 *
 * @param recipientId l'identifiant Discord du destinataire (un snowflake)
 * @param title       le titre de l'encart
 * @param body        le corps du message
 * @param footer      une mention de bas d'encart, facultative
 */
public record DirectMessageRequest(String recipientId, String title, String body, String footer) {
}
