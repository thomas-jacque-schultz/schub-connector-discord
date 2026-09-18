package schultz.thomas.discord.bot.data.enums;

/**
 * Les permissions dont les commandes slash ont besoin, telles que le <strong>cœur</strong> les
 * nomme.
 *
 * <p>Ce n'est pas une copie du modèle de droits et il ne faut pas en faire une : le cœur en
 * connaît une douzaine, ce connecteur n'en cite que quatre — celles que ses commandes réclament.
 * Un connecteur qui se mettrait à tenir le catalogue complet des permissions serait un
 * connecteur qui a recommencé à stocker du métier (migration §4).</p>
 *
 * <p>Ce sont des valeurs de <em>contrat</em>, au même titre qu'un slug : elles doivent
 * correspondre exactement aux constantes de l'enum {@code Permission} du cœur, qui est seul à
 * décider ce qu'elles ouvrent.</p>
 */
public enum PermissionEnum {

    SERVER_VIEW,
    SERVER_START,
    SERVER_STOP,
    DISCORD_CHANNEL_MANAGE
}
