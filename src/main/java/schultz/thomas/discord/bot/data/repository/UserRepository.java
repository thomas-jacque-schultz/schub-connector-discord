package schultz.thomas.discord.bot.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import schultz.thomas.discord.bot.data.entity.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    UserEntity findByDiscordId(String discordId);

    Optional<UserEntity> findByAuthUsername(String authUsername);
}
