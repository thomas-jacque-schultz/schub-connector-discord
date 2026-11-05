# schub-connector-discord

Le bot Discord de **Schub** : il pilote les serveurs de jeu de la communauté Miam depuis Discord et tient à jour leurs messages dans les salons abonnés. PremadeLab a sa propre application Discord (`schub-connector-discord-premadelab`).

## Format d'une commande

    /[command] [option1] [option2] [option3]
    [
        payloadLine1 : value1
        payloadLine2 : value2
        payloadLine3 : value3
    ]

Un identifiant Discord s'écrit comme `245281978054737920`.

## Ajouter le suivi d'un serveur de jeu

    identifier : [gaming-assetto]
    name : [Assetto Corsa]
    url : [assetto.schultz-thomas.fr]
    playersMax : [12]
    installation : [Download Modpack From Drive]
    version : [1.16]
    description : [Course à paramétrer sur l'interface web]
    admins : [Pseudo1 | Pseudo2]
