package com.ag13.mafia.rest.mafiaclientrest.domain;

import com.ag13.mafia.rest.mafiaclientrest.service.GameConfigService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    ConcurrentLinkedQueue<Message> allPlayerMessages;
    int timeRemainingInCurrentPhase;
    GameConfigService gameConfigService;

    public void tick() {
        if(currentPhase == Phase.WIN) {

            return;
        }
        // START -> ASSIGN ROLES
        if(currentPhase.equals(Phase.START)) {
            // assign the roles
            assignRandomRoles();
            printRoles();
//            var it = 1000;
//            var flag = false;
//            while(it-- > 0) {
//                assignRandomRoles();
//                for(var p : players.values()) {
//                    if(p.getName().equals("Manohar") && p.getRole() == Role.POLICE) {
//                        flag = true;
//                        break;
//                    }
//                }
//                if(flag) {
//                    break;
//                }
//            }
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

            var mostVotedPlayers = findMostVotedPlayer(votes);
            if(mostVotedPlayers == null || mostVotedPlayers.isEmpty()) {
                log.info("Nobody was voted at night");
                addAllPlayerMessage("Nobody died tonight");
            } else if(Objects.equals(mostVotedPlayers.getFirst().getKey(), doctorProtectedId)) {
                log.info("Player " + mostVotedPlayers + " is protected and cannot be killed");
                addAllPlayerMessage("Doctor has protected someone from dying");
            } else {
                log.info("Player " + mostVotedPlayers + " has been killed by mafia");
                players.get(mostVotedPlayers.getFirst().getKey()).setAlive(false);
                addAllPlayerMessage(players.get(mostVotedPlayers.getFirst().getKey()).getName() + " has been killed");
                if(evaluateWinConditions(null)) {
                    return;
                }
            }

            resetVotesOfAllPlayers();
            currentPhase = Phase.DAY_DISCUSSION;
            timeRemainingInCurrentPhase = gameConfigService.getDayDuration();
            return;
        }

        // DAY DISCUSSION PHASE
        if(currentPhase.equals(Phase.DAY_DISCUSSION)) {
            if(eligibleForPhaseSkip()) {
                currentPhase = Phase.DAY_VOTING;
                timeRemainingInCurrentPhase = gameConfigService.getDayVoteDuration();
                addAllPlayerMessage("Majority has voted to skip the discussion");
                addAllPlayerMessage("Get ready to vote. (Alteast " + ((int)Math.floor((double) getNumberOfPlayersAlive() / 2)) + " votes required)");
                return;
            }
            if(timeRemainingInCurrentPhase > 0) {
                timeRemainingInCurrentPhase--;
                return;
            }

            currentPhase = Phase.DAY_VOTING;
            timeRemainingInCurrentPhase = gameConfigService.getDayVoteDuration();
            addAllPlayerMessage("Get ready to vote. (Alteast " + ((int)Math.floor((double) getNumberOfPlayersAlive() / 2)) + " votes required)");
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
            if(mostVotedPlayer == null || mostVotedPlayer.isEmpty()) {
                log.info("Nobody was voted at day");
                daysWithoutVillageKill++;
                addAllPlayerMessage("Nobody died today");
            } else {
                int firstPlayerVotes = mostVotedPlayer.getFirst().getValue();
                int secondPlayerVotes = mostVotedPlayer.size() >= 2 ? mostVotedPlayer.get(1).getValue() : 0;
                var votesRequired = ((int)Math.floor((double) getNumberOfPlayersAlive() / 2));
                if(firstPlayerVotes >= votesRequired && firstPlayerVotes != secondPlayerVotes) {
                    log.info("Player " + mostVotedPlayer + " has been killed lynched");
                    daysWithoutVillageKill = 0;
                    addAllPlayerMessage(players.get(mostVotedPlayer.getFirst().getKey()).getName() + " has been killed");
                    players.get(mostVotedPlayer.getFirst().getKey()).setAlive(false);
                    if(evaluateWinConditions(mostVotedPlayer.getFirst().getKey())) {
                        return;
                    }
                } else if(firstPlayerVotes >= votesRequired) {
                    log.info("Tie in votes. Nobody has been killed");
                    addAllPlayerMessage("Tie in votes. Nobody has been killed");
                } else {
                    log.info("Not enough votes to lynch " + mostVotedPlayer);
                    daysWithoutVillageKill++;
                    addAllPlayerMessage(players.get(mostVotedPlayer.getFirst().getKey()).getName() + " could not be killed due to only " + mostVotedPlayer.getFirst().getValue() + " / " + votesRequired + " votes");
                }
            }

            resetVotesOfAllPlayers();
            dayCount++;
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

    private void printRoles() {
        for(Player player : players.values()) {
            log.info("{} - {}", player.getName(), player.getRole());
        }
    }


    public void addAllPlayerMessage(String message) {
        allPlayerMessages.add(new Message(0, Instant.now().toEpochMilli(), message));
    }

    private boolean eligibleForPhaseSkip() {
        var skipVotes = 0;
        for(Player player : players.values()) {
            skipVotes += player.hasVotedToSkipPhase ? 1 : 0;
        }

        var alivePlayersCount = getNumberOfPlayersAlive();
        return skipVotes >= Math.ceil((double) alivePlayersCount / 2);
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

    private void resetVotesOfAllPlayers() {
        for(Player player : players.values()) {
            player.setNightTargetPlayerId(null);
            player.setVotedForPlayerId(null);
            player.setInspectedTonight(false);
            player.setHasVotedToSkipPhase(false);
        }
    }

    private List<Map.Entry<String, Integer>> findMostVotedPlayer(List<String> list) {
        if(list == null || list.isEmpty()) {
            return null;
        }
        Map<String, Integer> map = new HashMap<>();
        for (String t : list) {
            map.put(t, map.getOrDefault(t, 0) + 1);
        }

        return map.entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .toList();
    }

    private boolean evaluateWinConditions(String lynchedPlayerId) {
        var mafiaCount = players.values().stream().filter(p -> p.isAlive() && p.getRole() == Role.MAFIA).count();
        var nonMafiaCount = players.values().stream().filter(p -> p.isAlive() && p.getRole() != Role.MAFIA).count();

        if (mafiaCount == 0) {
            // villagers wins, stop the game
            currentPhase = Phase.WIN;
            result = GameResult.VILLAGERS_WIN;
            addAllPlayerMessage("Villagers win");
            return true;
        }

        // lynched player is fool -> fool wins
        if (lynchedPlayerId != null && players.get(lynchedPlayerId) != null && players.get(lynchedPlayerId).getRole() == Role.FOOL) {
            // lynchedPlayerId wins, stop the game
            currentPhase = Phase.WIN;
            result = GameResult.FOOL_WIN;
            addAllPlayerMessage("Fool wins");
            return true;
        }

        var headhunterWithTarget = players.values()
                .stream()
                .filter(p -> p.isAlive() && p.getRole() == Role.HEADHUNTER)
                .filter(p -> Objects.equals(p.getHeadhunterTargetPlayerId(), lynchedPlayerId)).toList();
        if (!headhunterWithTarget.isEmpty()) {
            // lynched player is hh target -> hh wins
            // hh wins, stop the game
            currentPhase = Phase.WIN;
            result = GameResult.HEADHUNTER_WIN;
            addAllPlayerMessage("Headhunter wins");
            return true;
        }


        if (mafiaCount >= nonMafiaCount) {
            // number of mafia >= villagers -> mafia wins, stop the game
            currentPhase = Phase.WIN;
            result = GameResult.MAFIA_WIN;
            addAllPlayerMessage("Mafia wins");
            return true;
        }

        return false;
    }


    private void assignRandomRoles() {
        var roles = new ArrayList<Role>(List.of(
                Role.POLICE,
                Role.DOCTOR
        ));

        if(gameConfigService.isFoolEnabled()) {
            roles.add(Role.FOOL);
        }

        if(gameConfigService.isHeadhunterEnabled()) {
            roles.add(Role.HEADHUNTER);
        }

        var remaining = players.size() - roles.size();
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
                       hhPlayer.setHeadhunterTargetPlayerId(list.get(i).id);
                       // WARNING -->>>> Logic exits here. remove this before adding further logic
                       return;
                   }
               }
           }
        }
    }
}
