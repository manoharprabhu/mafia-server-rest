package com.ag13.mafia.rest.mafiaclientrest.domain.statemachine;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.Player;
import com.ag13.mafia.rest.mafiaclientrest.domain.Role;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class StartState implements IState {
    private final Game game;
    public StartState(Game game) {
        this.game = game;
    }

    @Override
    public Phase execute() {
        // assign the roles
        assignRandomRoles();
        appendNumberToNames();
//            var it = 1000;
//            var flag = false;
//            while(it-- > 0) {
//                assignRandomRoles();
//                for(var p : game.getPlayers().values()) {
//                    if(p.getName().equals("Manohar") && p.getRole() == Role.HEADHUNTER) {
//                        flag = true;
//                        break;
//                    }
//                }
//                if(flag) {
//                    break;
//                }
//            }
        printRoles();
        log.info(game.getPlayers().values().toString());
        game.setTimeRemainingInCurrentPhase(game.getGameConfigService().getNightVoteDuration());
        game.addAllPlayerMessage("Starting the game with " + game.getPlayers().size() + " players");
        return Phase.NIGHT;
    }

    // Make it easy to remember and locate people on the grid
    private void appendNumberToNames() {
        var allPlayers = new ArrayList<>(game.getPlayers().values());
        for(var i = 0; i < allPlayers.size(); i++) {
            allPlayers.get(i).setName((i + 1) + " " + allPlayers.get(i).getName());
        }
    }

    private void printRoles() {
        for(Player player : game.getPlayers().values()) {
            log.info("{} - {}", player.getName(), player.getRole());
        }
    }

    private void assignRandomRoles() {
        var roles = new ArrayList<Role>(List.of(
                Role.POLICE,
                Role.DOCTOR
        ));

        if(game.getGameConfigService().isFoolEnabled()) {
            roles.add(Role.FOOL);
        }

        if(game.getGameConfigService().isHeadhunterEnabled()) {
            roles.add(Role.HEADHUNTER);
        }

        var players = game.getPlayers();
        var remaining = players.size() - roles.size();
        var mafiaSize = remaining / 2;
        for(var i = 0; i < mafiaSize; i++) {
            roles.add(Role.MAFIA);
        }
        for(var i = 0; i < remaining - mafiaSize; i++) {
            roles.add(Role.VILLAGER);
        }

        //Reset hhtarget and godfather flag for all players
        for(Player player : players.values()) {
            player.setHeadhunterTargetPlayerId(null);
            player.setGodFather(false);
        }

        Collections.shuffle(roles);
        var index = 0;
        for(var key : players.keySet()) {
            players.get(key).setRole(roles.get(index++));
        }

        // make one of the mafia as godfather
        for(var key : players.keySet()) {
            if(players.get(key).getRole() == Role.MAFIA) {
                players.get(key).setGodFather(true);
                break;
            }
        }

        // assign target for headhunter
        for(var key : players.keySet()) {
            if(players.get(key).getRole() == Role.HEADHUNTER) {
                var iteration = 100;
                var hhPlayer = players.get(key);
                while(iteration-- > 0) {
                    var list = new ArrayList<>(players.values());
                    var i = ((int)(Math.random() * 100)) % players.size();
                    if(list.get(i).getRole() == Role.VILLAGER || list.get(i).getRole() == Role.POLICE || list.get(i).getRole() == Role.DOCTOR) {
                        hhPlayer.setHeadhunterTargetPlayerId(list.get(i).getId());
                        // WARNING -->>>> Logic exits here. remove this before adding further logic
                        return;
                    }
                }
            }
        }
    }
}
