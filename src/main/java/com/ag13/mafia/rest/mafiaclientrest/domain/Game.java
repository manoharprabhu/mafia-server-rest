package com.ag13.mafia.rest.mafiaclientrest.domain;

import com.ag13.mafia.rest.mafiaclientrest.service.GameConfigService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Slf4j
@ToString
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
        log.info("tick");
        if(currentPhase.equals(Phase.START)) {
            // assign the roles
            assignRandomRoles();
            log.info(players.values().toString());
            // change game state to start the game
            currentPhase = Phase.NIGHT;
        }
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

    private void assignRandomRoles() {
        var roles = new ArrayList<Role>(List.of(
                Role.POLICE,
                Role.FOOL,
                Role.HEADHUNTER,
                Role.DOCTOR
        ));
        var remaining = players.size() - 4;
        var mafiaSize = remaining / 2;
        for(var i = 0; i < mafiaSize; i++) {
            roles.add(Role.MAFIA);
        }
        for(var i = 0; i < remaining - mafiaSize; i++) {
            roles.add(Role.VILLAGER);
        }

        Collections.shuffle(roles);
        var index = 0;
        for(var key : players.keySet()) {
            players.get(key).setRole(roles.get(index++));
        }
    }
}
