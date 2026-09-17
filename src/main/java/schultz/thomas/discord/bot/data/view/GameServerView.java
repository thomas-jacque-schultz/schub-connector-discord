package schultz.thomas.discord.bot.data.view;

import lombok.Data;

import java.util.List;

/**
 * Un GameServer tel que le cœur le rapporte, réduit à ce qu'un message Discord affiche.
 *
 * <p>Ce n'est <strong>pas</strong> une entité : rien n'est persisté ici. Le connecteur ne
 * détient aucune projection du domaine, il tire périodiquement (plan §5). Le libellé et l'icône
 * du jeu viennent du cœur plutôt que d'une énumération locale — le connecteur n'a pas à
 * connaître le catalogue des jeux.</p>
 */
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
    private List<String> admins;
    /** ONLINE / OFFLINE / UNREACHABLE / UNKNOWN, ou null si jamais observé. */
    private String status;

    public boolean isOnline() {
        return "ONLINE".equals(status);
    }
}
