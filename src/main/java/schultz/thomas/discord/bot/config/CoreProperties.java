package schultz.thomas.discord.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Comment joindre le cœur, et à quelle cadence en tirer l'état. */
@Data
@ConfigurationProperties(prefix = "core")
public class CoreProperties {

    private String baseUrl = "http://schub-core:8080";

    /**
     * Cadence du pull. C'est la garantie de correction du système : un push perdu n'a aucune
     * conséquence, le pull suivant resynchronise (plan §5).
     */
    private Duration pullInterval = Duration.ofMinutes(1);

    private Duration connectTimeout = Duration.ofSeconds(3);

    private Duration readTimeout = Duration.ofSeconds(10);
}
