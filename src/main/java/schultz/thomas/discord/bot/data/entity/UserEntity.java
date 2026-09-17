package schultz.thomas.discord.bot.data.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import schultz.thomas.discord.bot.data.enums.UserPrivilegeEnum;

@Data
@Document( collection = "users" )
public class UserEntity {

    @Id
    private String id;
    private String discordId;
    private String discordUsername;
    private String authUsername;
    private String passwordHash;
    private UserPrivilegeEnum privilege;

}
