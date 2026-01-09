package com.ag13.mafia.rest.mafiaclientrest.domain.statemachine;

import com.ag13.mafia.rest.mafiaclientrest.domain.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ResolveNightState implements IState {
    private final Game game;

    public ResolveNightState(Game game) {
        this.game = game;
    }

    @Override
    public Phase execute() {
        var players = game.getPlayers();

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

        var mostVotedPlayers = game.findMostVotedPlayer(votes);
        if(mostVotedPlayers == null || mostVotedPlayers.isEmpty()) {
            log.info("Nobody was voted at night");
            game.addAllPlayerMessage("Nobody died tonight");
        } else if(Objects.equals(mostVotedPlayers.getFirst().getKey(), doctorProtectedId)) {
            log.info("Player {} is protected and cannot be killed", mostVotedPlayers);
            game.addAllPlayerMessage("Doctor has protected someone from dying");
        } else {
            log.info("Player {} has been killed by mafia", mostVotedPlayers);
            players.get(mostVotedPlayers.getFirst().getKey()).setAlive(false);
            game.addAllPlayerMessage(players.get(mostVotedPlayers.getFirst().getKey()).getName() + " has been killed");
            var winner = game.evaluateWinConditions(null);
            game.setResult(winner);
            if(winner != GameResult.NONE) {
                return Phase.WIN;
            }
        }

        game.resetVotesOfAllPlayers();
        game.setTimeRemainingInCurrentPhase(game.getGameConfigService().getDayDuration());
        return Phase.DAY_DISCUSSION;
    }
}
