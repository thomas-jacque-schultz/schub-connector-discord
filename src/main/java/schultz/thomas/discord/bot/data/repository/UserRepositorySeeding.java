package schultz.thomas.discord.bot.data.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import schultz.thomas.discord.bot.data.entity.UserEntity;
import schultz.thomas.discord.bot.data.enums.UserPrivilegeEnum;

import java.util.Objects;

@Configuration
public class UserRepositorySeeding {

    public UserRepository userRepository;

    @Value("${discord.admin.id}")
    private String adminDiscordId;

    @Value("${discord.admin.username}")
    private String adminDiscordUsername;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            UserEntity admin = new UserEntity();
            admin.setDiscordId(adminDiscordId);
            admin.setDiscordUsername(adminDiscordUsername);
            admin.setPrivilege(UserPrivilegeEnum.OWNER);
            UserEntity existingUser = userRepository.findByDiscordId(admin.getDiscordId());
            if (Objects.isNull(existingUser)) {
                userRepository.save(admin);
                System.out.println("Admin user created.");
            }
        };
    }
}
