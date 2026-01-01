package com.ag13.mafia.rest.mafiaclientrest.domain;

import com.ag13.mafia.rest.mafiaclientrest.service.GameConfigService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        // START -> ASSIGN ROLES
        if(currentPhase.equals(Phase.START)) {
            // assign the roles
            assignRandomRoles();
            log.info(players.values().toString());
            // change game state to start the game
            currentPhase = Phase.NIGHT;
            timeRemainingInCurrentPhase = gameConfigService.getNightVoteDuration();
            addAllPlayerMessage("Starting the game with " + players.size() + " players");
            return;
        }

        // NIGHT VOTING PHASE
        if(currentPhase.equals(Phase.NIGHT)) {
            if(timeRemainingInCurrentPhase > 0) {
                timeRemainingInCurrentPhase--;
                return;
            }

            currentPhase = Phase.RESOLVE_NIGHT;
            return;
        }

        //RESOLVE_NIGHT -> tally mafia votes, kill the target if they are not protected by doctor
        if(currentPhase.equals(Phase.RESOLVE_NIGHT)) {
            var votes = new ArrayList<String>();
            String doctorProtectedId = null;
            for(Player player : players.values()) {
                if(player.getRole().equals(Role.MAFIA) && (player.getNightTargetPlayerId() != null && !player.getNightTargetPlayerId().isEmpty())) {
                    votes.add(player.getNightTargetPlayerId());
                }

                if(player.getRole().equals(Role.DOCTOR) && (player.getNightTargetPlayerId() != null && !player.getNightTargetPlayerId().isEmpty())) {
                    doctorProtectedId = player.getNightTargetPlayerId();
                }
            }

            var mostVotedPlayer = findMostVotedPlayer(votes);
            if(mostVotedPlayer == null) {
                log.info("Nobody was voted at night");
                addAllPlayerMessage("Nobody died tonight");
            } else if(Objects.equals(mostVotedPlayer.getKey(), doctorProtectedId)) {
                log.info("Player " + mostVotedPlayer + " is protected and cannot be killed");
                addAllPlayerMessage("Nobody died tonight");
            } else {
                log.info("Player " + mostVotedPlayer + " has been killed by mafia");
                players.get(mostVotedPlayer.getKey()).setAlive(false);
                addAllPlayerMessage(players.get(mostVotedPlayer.getKey()).getName() + " has been killed");
            }

            resetVotesOfAllPlayers();
            currentPhase = Phase.DAY_DISCUSSION;
            timeRemainingInCurrentPhase = gameConfigService.getDayDuration();
            return;
        }

        // DAY DISCUSSION PHASE
        if(currentPhase.equals(Phase.DAY_DISCUSSION)) {
            if(timeRemainingInCurrentPhase > 0) {
                timeRemainingInCurrentPhase--;
                return;
            }

            currentPhase = Phase.DAY_VOTING;
            timeRemainingInCurrentPhase = gameConfigService.getDayVoteDuration();
            addAllPlayerMessage("Get ready to vote. (Alteast " + ((int)Math.ceil((double) getNumberOfPlayersAlive() / 2)) + " votes required)");
            return;
        }

        // DAY VOTING PHASE
        if(currentPhase.equals(Phase.DAY_VOTING)) {
            if(timeRemainingInCurrentPhase > 0) {
                timeRemainingInCurrentPhase--;
                return;
            }

            currentPhase = Phase.RESOLVE_DAY;
            return;
        }

        // RESOLVE DAY PHASE
        if(currentPhase.equals(Phase.RESOLVE_DAY)) {
            var votes = new ArrayList<String>();
            for(Player player : players.values()) {
                if((player.getVotedForPlayerId() != null && !player.getVotedForPlayerId().isEmpty())) {
                    votes.add(player.getVotedForPlayerId());
                }
            }

            var mostVotedPlayer = findMostVotedPlayer(votes);
            if(mostVotedPlayer == null) {
                log.info("Nobody was voted at day");
                addAllPlayerMessage("Nobody died today");
            } else {
                var votesRequired = ((int)Math.ceil((double) getNumberOfPlayersAlive() / 2));
                if(mostVotedPlayer.getValue() >= votesRequired) {
                    log.info("Player " + mostVotedPlayer + " has been killed lynched");
                    addAllPlayerMessage(players.get(mostVotedPlayer.getKey()).getName() + " has been killed");
                    players.get(mostVotedPlayer.getKey()).setAlive(false);
                } else {
                    log.info("Not enough votes to lynch " + mostVotedPlayer);
                    addAllPlayerMessage(players.get(mostVotedPlayer.getKey()).getName() + " could not be killed due to only " + mostVotedPlayer.getValue() + " / " + votesRequired + " votes");
                }
            }

            resetVotesOfAllPlayers();
            currentPhase = Phase.NIGHT;
            timeRemainingInCurrentPhase = gameConfigService.getNightVoteDuration();
            return;
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

    private int getNumberOfPlayersAlive() {
        var count = 0;
        for(Player player : players.values()) {
            if(player.isAlive()) {
                count++;
            }
        }

        return count;
    }

    private void addAllPlayerMessage(String message) {
        allPlayerMessages.add(new Message(Instant.now().toEpochMilli(), message));
    }

    private void resetVotesOfAllPlayers() {
        for(Player player : players.values()) {
            player.setNightTargetPlayerId(null);
            player.setVotedForPlayerId(null);
        }
    }

    private Map.Entry<String, Integer> findMostVotedPlayer(List<String> list) {
        if(list == null || list.isEmpty()) {
            return null;
        }
        Map<String, Integer> map = new HashMap<>();
        for (String t : list) {
            // Puts the element in the map and increments its count
            map.put(t, map.getOrDefault(t, 0) + 1);
        }

        Map.Entry<String, Integer> max = null;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (max == null || entry.getValue() > max.getValue()) {
                max = entry;
            }
        }

        return max;
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
