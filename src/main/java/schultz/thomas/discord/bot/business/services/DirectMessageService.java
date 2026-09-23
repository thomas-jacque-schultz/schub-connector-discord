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

@Slf4j
@RequiredArgsConstructor
@Service
public class DirectMessageService {

    private static final Duration DISCORD_TIMEOUT = Duration.ofSeconds(10);

    private static final int TITLE_LIMIT = 256;
    private static final int BODY_LIMIT = 4096;

    private final JDA jda;
    private final DiscordMessageService discordMessageService;

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
            Thread.currentThread().interrupt();
            log.warn("Envoi du message privé interrompu pour {}", request.recipientId());
            return fallback(embed, request);
        } catch (Exception failure) {
            log.warn("Message privé refusé pour {} ({}), repli sur un salon abonné",
                    request.recipientId(), failure.getMessage());
            return fallback(embed, request);
        }
    }

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

    private static String truncate(String value, int limit) {
        if (value == null) {
            return "";
        }
        return value.length() <= limit ? value : value.substring(0, limit - 1) + "…";
    }
}
