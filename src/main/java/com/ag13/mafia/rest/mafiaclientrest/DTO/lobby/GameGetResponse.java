package com.ag13.mafia.rest.mafiaclientrest.DTO.lobby;

import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/*
{
  "phase": "DAY_VOTING",
  "dayNumber": 2,
  "timeRemainingSeconds": 38,

  "you": {
    "playerId": "p4",
    "name": "Alice",
    "role": "POLICE",
    "alive": true,
    "hasActed": false
  },

  "players": [
    { "playerId": "p1", "name": "Bob", "alive": true },
    { "playerId": "p2", "name": "Carol", "alive": true },
    { "playerId": "p3", "name": "Dan", "alive": false }
  ],

  "allowedActions": ["VOTE"],

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
    private List<String> messages;
    private String gameResult;

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
}
