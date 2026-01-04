package com.ag13.mafia.rest.mafiaclientrest.DTO.game;

import com.ag13.mafia.rest.mafiaclientrest.domain.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
{
  "phase": "DAY_VOTING",
  "dayNumber": 2,
  "timeRemainingSeconds": 38,

  "you": {
    "playerId": "p4",
    "name": "Alice",
    "role": "POLICE",
    "alive": true
  },

  "players": [
    { "playerId": "p1", "name": "Bob", "alive": true },
    { "playerId": "p2", "name": "Carol", "alive": true },
    { "playerId": "p3", "name": "Dan", "alive": false }
  ],

  "messages": [
    "Dan was killed last night"
  ],

  "gameResult": null
}
 */
@Getter
@Setter
@ToString
public class GameGetResponse {
    private Phase phase;
    private int dayNumber;
    private int timeRemainingSeconds;
    private You you;
    private List<Player> players;
    private List<Message> messages;
    private Map<String, String> voteMap;
    private Map<String, Role> visibleRoles;
    private List<InspectionResult>  inspectionResults;
    private String yourHeadhunterTarget;
    private GameResult winner;
    private boolean hasInspectedAlready;

    @Getter
    @Setter
    public static class You {
        private String playerId;
        private String name;
        private Role role;
        private boolean alive;
    }

    @Getter
    @Setter
    @ToString
    public static class Player {
        private String playerId;
        private String name;
        private boolean alive;

        public static Player create(com.ag13.mafia.rest.mafiaclientrest.domain.Player player) {
            var result = new Player();
            result.setPlayerId(player.getId());
            result.setName(player.getName());
            result.setAlive(player.isAlive());
            return result;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Message {
        private long timestamp;
        private String message;

        public static Message create(com.ag13.mafia.rest.mafiaclientrest.domain.Message m) {
            var result = new Message();
            result.setMessage(m.getMessage());
            result.setTimestamp(m.getTimestamp());
            return result;
        }

        public static List<Message> create(List<com.ag13.mafia.rest.mafiaclientrest.domain.Message> messages) {
            var result = new ArrayList<Message>();
            for(var m : messages) {
                result.add(create(m));
            }

            return result;
        }
    }
}
