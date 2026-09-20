package schultz.thomas.discord.bot.business.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;
import schultz.thomas.discord.bot.api.dto.DirectMessageAck;
import schultz.thomas.discord.bot.api.dto.DirectMessageRequest;
import schultz.thomas.discord.bot.data.entity.ChannelEntity;

import java.awt.Color;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * L'écriture en message privé — la capacité qui manquait au connecteur.
 *
 * <p>Il savait écrire dans des <em>salons</em> ({@link DiscordMessageService}) et nulle part
 * ailleurs. Écrire à une personne demande un autre chemin : récupérer l'utilisateur, lui ouvrir
 * un canal privé, puis écrire dedans.</p>
 *
 * <h2>Les deux échecs silencieux, et pourquoi il y a un repli</h2>
 *
 * <p>Ce chemin échoue de deux façons qui ne ressemblent pas à des pannes :</p>
 * <ol>
 *   <li>le bot doit <strong>partager une guilde</strong> avec le destinataire. Sans cela, Discord
 *       refuse l'ouverture du canal privé ;</li>
 *   <li>le destinataire doit <strong>accepter les messages privés des membres de cette
 *       guilde</strong>. C'est un réglage de son compte, que le bot ne voit pas et ne peut pas
 *       changer. Il peut être modifié n'importe quand, sans prévenir personne.</li>
 * </ol>
 *
 * <p>Les deux se traduisent par un refus au moment de l'envoi, pas avant. Un formulaire de
 * contact branché dessus sans filet perdrait donc des messages en silence, et ne le découvrirait
 * qu'en constatant, un jour, qu'on ne reçoit plus rien. <strong>Un formulaire de contact qui perd
 * des messages est pire que pas de formulaire</strong> : il promet une réponse qui ne viendra
 * jamais.</p>
 *
 * <p>D'où le repli : si le message privé échoue, on poste dans un salon abonné. C'est moins
 * discret, et c'est exactement le compromis voulu — un message visible vaut mieux qu'un message
 * perdu.</p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DirectMessageService {

    /**
     * Le temps qu'on accorde à l'aller-retour Discord.
     *
     * <p>L'envoi est <strong>synchrone</strong>, et c'est délibéré : l'appelant veut savoir s'il
     * peut annoncer au visiteur que son message est parti. Une réponse optimiste suivie d'un
     * échec silencieux serait le défaut même qu'on cherche à éviter ici.</p>
     *
     * <p>Dix secondes couvrent un aller-retour normal, y compris une attente de limitation de
     * débit ; au-delà, on bascule sur le repli plutôt que de faire patienter davantage.</p>
     */
    private static final Duration DISCORD_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Les limites d'un encart Discord, appliquées ici plutôt que découvertes là-bas.
     *
     * <p>Discord refuse un encart dont le titre dépasse 256 caractères ou la description
     * 4096. Sans cette coupe, un message trop long serait refusé par l'API — et partirait
     * donc au repli, qui le refuserait pour exactement la même raison. Le message serait
     * perdu à cause de sa longueur, en ayant l'air d'un compte qui ferme ses MP.</p>
     */
    private static final int TITLE_LIMIT = 256;
    private static final int BODY_LIMIT = 4096;

    private final JDA jda;
    private final DiscordMessageService discordMessageService;

    /**
     * Écrit au destinataire, et à défaut dans un salon abonné.
     *
     * @return ce qu'il est advenu du message — jamais une exception : l'appelant a besoin de
     *         savoir <em>par où</em> c'est passé, pas de gérer une pile d'erreurs JDA.
     */
    public DirectMessageAck send(DirectMessageRequest request) {
        MessageEmbed embed = toEmbed(request);

        try {
            User recipient = jda.retrieveUserById(request.recipientId())
                    .submit()
                    .get(DISCORD_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

            recipient.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessageEmbeds(embed))
                    .submit()
                    .get(DISCORD_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

            log.info("Message privé remis à {}", request.recipientId());
            return DirectMessageAck.direct();
        } catch (InterruptedException interrupted) {
            // Ne jamais avaler une interruption : on repose le drapeau et on tente quand même le
            // repli, parce que le message, lui, n'a rien demandé.
            Thread.currentThread().interrupt();
            log.warn("Envoi du message privé interrompu pour {}", request.recipientId());
            return fallback(embed, request);
        } catch (Exception failure) {
            // Le cas courant n'est pas une panne : c'est un compte qui n'accepte pas les MP, ou
            // un bot qui ne partage plus de guilde avec lui. On le journalise comme un
            // avertissement — c'est une consigne d'exploitation, pas une erreur du code.
            log.warn("Message privé refusé pour {} ({}), repli sur un salon abonné",
                    request.recipientId(), failure.getMessage());
            return fallback(embed, request);
        }
    }

    /**
     * Le repli : le premier salon abonné qui accepte le message.
     *
     * <p>« Le premier » et non « tous » : ce message s'adresse à une personne. Le dupliquer dans
     * chaque salon abonné le transformerait en annonce publique, ce qui n'est pas ce qu'on
     * cherche — on cherche à ne pas le perdre.</p>
     *
     * <p>Si aucun salon n'est abonné, il n'y a plus de filet. On le dit alors très fort dans les
     * journaux, avec le contenu du message : c'est la dernière trace qui en restera.</p>
     */
    private DirectMessageAck fallback(MessageEmbed embed, DirectMessageRequest request) {
        for (ChannelEntity channel : discordMessageService.getSubscribedChannels()) {
            TextChannel textChannel = jda.getTextChannelById(channel.getChannelId());
            if (textChannel == null) {
                continue;
            }

            try {
                textChannel.sendMessageEmbeds(embed)
                        .submit()
                        .get(DISCORD_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

                log.info("Message remis par repli dans le salon {}", channel.getChannelId());
                return DirectMessageAck.fallback();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return DirectMessageAck.lost();
            } catch (Exception failure) {
                log.warn("Repli refusé par le salon {} : {}", channel.getChannelId(), failure.getMessage());
            }
        }

        log.error("""
                MESSAGE PERDU — ni message privé ni salon de repli n'ont accepté l'envoi.
                Destinataire : {}
                Titre : {}
                Corps : {}""", request.recipientId(), request.title(), request.body());
        return DirectMessageAck.lost();
    }

    private MessageEmbed toEmbed(DirectMessageRequest request) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(truncate(request.title(), TITLE_LIMIT))
                .setDescription(truncate(request.body(), BODY_LIMIT))
                .setColor(new Color(0x2F, 0xA0, 0x8F));

        if (request.footer() != null && !request.footer().isBlank()) {
            builder.setFooter(request.footer());
        }

        return builder.build();
    }

    /** Coupe en signalant la coupe : un texte tronqué en silence se lit comme un texte fini. */
    private static String truncate(String value, int limit) {
        if (value == null) {
            return "";
        }
        return value.length() <= limit ? value : value.substring(0, limit - 1) + "…";
    }
}
