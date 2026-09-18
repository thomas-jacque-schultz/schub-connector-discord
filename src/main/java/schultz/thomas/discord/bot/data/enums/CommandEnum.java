package schultz.thomas.discord.bot.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandEnum {

    SUSBSCRIBE_A_CHANNEL("subscribe"),                      // start following a channel for righting or listening messages

    UPDATE_ALL_EXISTING_MESSAGES("refresh"),               // update all existing messages for all gaming servers
    UPDATE_ALL_EXISTING_MESSAGES_SGAMING("refresh-server"),       // update all existing messages for one sGaming server

    START_SGAMING("start"),                              // start sGaming server
    STOP_SGAMING("pause"),                               // stop sGaming server

    // create-gaming-server et update-gaming-server ont été retirées (plan §4) : une fiche se
    // rédige, elle ne se dicte pas dans un chat. Le formulaire du front est plus complet, et
    // /create n'exposait même pas le déploiement à lier — un serveur créé ainsi était inutilisable.

    // create-user et update-user ont été retirées le 18-09 : le connecteur ne détient plus
    // d'utilisateurs. Attribuer un rôle depuis un chat contournerait la règle qui interdit
    // d'attribuer un rôle plus puissant que le sien, laquelle vit dans le cœur (plan §A.1).

    REFRESH_GAMING_SERVER_MESSAGE("refresh-status");

    private final String commandName;

}
