package com.ag13.mafia.rest.mafiaclientrest.domain;

import com.ag13.mafia.rest.mafiaclientrest.service.GameConfigService;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Game {
    String lobbyId;
    ConcurrentHashMap<String, Player> players;
    Phase currentPhase;
    int dayCount;
    int daysWithoutVillageKill;
    GameResult result;
    List<Message> allPlayerMessages;
    int timeRemainingInCurrentPhase;
    GameConfigService gameConfigService;

    public void tick() {
        // assign the roles
        // change game state to start the game
        // NIGHT: // night voting phase set time remaining to NIGHT time
        // When countdown reaches 0, tally mafia votes and decide victim
        // check if victim is protected by doctor before killing them
        // kill them if required
        // Check for win conditions -> goto WIN if yes
        // Phase goes from NIGHT to DAY
        // set time remaining to DAY_DISCUSSION and let it run down
        // Phase goes from DAY to DAYVOTING and set time remaining to DAY_VOTING
        // let timer run down and collect votes
        // kill player if majority votes
        // check for win condition -> goto WIN if yes
        // goto NIGHT
    }
}
