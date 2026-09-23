package schultz.thomas.discord.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "core")
public class CoreProperties {

    private String baseUrl = "http://schub-core:8080";

    private Duration pullInterval = Duration.ofMinutes(1);

    private Duration connectTimeout = Duration.ofSeconds(3);

    private Duration readTimeout = Duration.ofSeconds(10);
}
