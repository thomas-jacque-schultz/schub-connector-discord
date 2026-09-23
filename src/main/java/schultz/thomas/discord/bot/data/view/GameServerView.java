package schultz.thomas.discord.bot.data.view;

import lombok.Data;

@Data
public class GameServerView {

    private String id;
    private String slug;
    private String name;
    private String urlConnection;
    private String gameLabel;
    private String gameIconUrl;
    private Integer playersMax;
    private String installation;
    private String version;
    private String description;
    private String status;

    public boolean isOnline() {
        return "ONLINE".equals(status);
    }
}
