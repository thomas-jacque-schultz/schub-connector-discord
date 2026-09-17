package schultz.thomas.discord.bot.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import schultz.thomas.discord.bot.data.entity.ChannelEntity;

@Repository
public interface ChannelRepository extends MongoRepository<ChannelEntity, String> {
}
