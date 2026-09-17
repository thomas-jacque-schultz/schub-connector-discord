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

    private String channelId;   // ref to discord channel id
    private String name;        // ref to discord channel name
    private String guildId;     // ref to discord server id

    private List<MessageEntity> messages;
}
