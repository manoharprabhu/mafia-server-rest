package com.ag13.mafia.rest.mafiaclientrest.domain;

import com.ag13.mafia.rest.mafiaclientrest.domain.statemachine.StateMachine;
import com.ag13.mafia.rest.mafiaclientrest.service.GameConfigService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.util.Tuple;

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

    public void addAllPlayerMessage(String message) {
        allPlayerMessages.add(new Message(0, Instant.now().toEpochMilli(), message));
    }

    public int getNumberOfPlayersAlive() {
        var count = 0;
        for(Player player : players.values()) {
            if(player.isAlive()) {
                count++;
            }
        }

        return count;
    }

    public void resetVotesOfAllPlayers() {
        for(Player player : players.values()) {
            player.setNightTargetPlayerId(null);
            player.setVotedForPlayerId(null);
            player.setInspectedTonight(false);
            player.setHasVotedToSkipPhase(false);
        }
    }

    public List<Map.Entry<String, Integer>> findMostVotedPlayer(List<String> list) {
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

    public GameResult evaluateWinConditions(String lynchedPlayerId) {
        var mafiaCount = players.values().stream().filter(p -> p.isAlive() && p.getRole() == Role.MAFIA).count();
        var nonMafiaCount = players.values().stream().filter(p -> p.isAlive() && p.getRole() != Role.MAFIA).count();

        if (mafiaCount == 0) {
            // villagers wins, stop the game
            addAllPlayerMessage("Villagers win");
            return GameResult.VILLAGERS_WIN;
        }

        // lynched player is fool -> fool wins
        if (lynchedPlayerId != null && players.get(lynchedPlayerId) != null && players.get(lynchedPlayerId).getRole() == Role.FOOL) {
            // lynchedPlayerId wins, stop the game
            addAllPlayerMessage("Fool wins");
            return GameResult.FOOL_WIN;
        }

        var headhunterWithTarget = players.values()
                .stream()
                .filter(p -> p.isAlive() && p.getRole() == Role.HEADHUNTER)
                .filter(p -> Objects.equals(p.getHeadhunterTargetPlayerId(), lynchedPlayerId)).toList();
        if (!headhunterWithTarget.isEmpty()) {
            // lynched player is hh target -> hh wins
            // hh wins, stop the game
            addAllPlayerMessage("Headhunter wins");
            return GameResult.HEADHUNTER_WIN;
        }


        if (mafiaCount >= nonMafiaCount) {
            // number of mafia >= villagers -> mafia wins, stop the game
            addAllPlayerMessage("Mafia wins");
            return GameResult.MAFIA_WIN;
        }

        return GameResult.NONE;
    }
}
