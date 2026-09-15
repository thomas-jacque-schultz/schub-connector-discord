package schultz.thomas.discord.bot.model.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import schultz.thomas.discord.bot.model.enums.GamesNameEnum;
import schultz.thomas.discord.bot.model.enums.ServerStatusEnum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Document( collection = "servers" )
public  class GamingServerEntity {

   @Id
   private String id;            // auto generated uuid
   private String identifier;    // foreign key to other software
   private Integer portainerStackId;
   private String name;
   private String urlConnection;
   private GamesNameEnum gameName;
   private Integer playersMax;
   private String installation;
   private String version;
   private String description;
   private List<String> admins = new ArrayList<>(); // evol to list of userEntity

   /** ports à ouvrir sur la Freebox tant que ce serveur tourne ; vide = aucune redirection pilotée */
   private List<GamingServerPort> ports = new ArrayList<>();

   /** null = never checked yet; drives the "force update on first poll" logic */
   private ServerStatusEnum status;

   /** updated on every Portainer check, regardless of status change */
   private Instant lastStatusCheckAt;

   /** timestamp when current status started */
   private Instant lastStatusChangeAt;

   /** chronological status history; each entry marks the start of a status segment */
   private List<GamingServerStatusHistoryEntry> statusHistory = new ArrayList<>();

}
