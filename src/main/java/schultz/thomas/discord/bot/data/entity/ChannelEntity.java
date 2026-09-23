package schultz.thomas.discord.bot.data.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document( collection = "channels" )
public class ChannelEntity {

    @Id
    private String id;

    private String channelId;
    private String name;
    private String guildId;

    private List<MessageEntity> messages;
}
