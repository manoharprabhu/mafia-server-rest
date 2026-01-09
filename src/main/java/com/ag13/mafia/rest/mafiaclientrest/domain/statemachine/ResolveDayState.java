package com.ag13.mafia.rest.mafiaclientrest.domain.statemachine;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.GameResult;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.Player;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class ResolveDayState implements IState{
    private final Game game;

    public ResolveDayState(Game game) {
        this.game = game;
    }
    @Override
    public Phase execute() {
        var players = game.getPlayers();
        var votes = new ArrayList<String>();
        for(Player player : players.values()) {
            if((player.getVotedForPlayerId() != null && !player.getVotedForPlayerId().isEmpty())) {
                votes.add(player.getVotedForPlayerId());
            }
        }

        var mostVotedPlayer = game.findMostVotedPlayer(votes);
        if(mostVotedPlayer == null || mostVotedPlayer.isEmpty()) {
            log.info("Nobody was voted at day");
            game.setDaysWithoutVillageKill(game.getDaysWithoutVillageKill() + 1);
            game.addAllPlayerMessage("Nobody died today");
        } else {
            int firstPlayerVotes = mostVotedPlayer.getFirst().getValue();
            int secondPlayerVotes = mostVotedPlayer.size() >= 2 ? mostVotedPlayer.get(1).getValue() : 0;
            var votesRequired = ((int)Math.floor((double) game.getNumberOfPlayersAlive() / 2));
            if(firstPlayerVotes >= votesRequired && firstPlayerVotes != secondPlayerVotes) {
                log.info("Player {} has been killed lynched", mostVotedPlayer);
                game.setDaysWithoutVillageKill(0);
                game.addAllPlayerMessage(players.get(mostVotedPlayer.getFirst().getKey()).getName() + " has been killed");
                players.get(mostVotedPlayer.getFirst().getKey()).setAlive(false);
                var winner = game.evaluateWinConditions(mostVotedPlayer.getFirst().getKey());
                game.setResult(winner);
                if(winner != GameResult.NONE) {
                    return Phase.WIN;
                }
            } else if(firstPlayerVotes >= votesRequired) {
                log.info("Tie in votes. Nobody has been killed");
                game.addAllPlayerMessage("Tie in votes. Nobody has been killed");
            } else {
                log.info("Not enough votes to lynch {}", mostVotedPlayer);
                game.setDaysWithoutVillageKill(game.getDaysWithoutVillageKill() + 1);
                game.addAllPlayerMessage(players.get(mostVotedPlayer.getFirst().getKey()).getName() + " could not be killed due to only " + mostVotedPlayer.getFirst().getValue() + " / " + votesRequired + " votes");
            }
        }

        game.resetVotesOfAllPlayers();
        game.setDayCount(game.getDayCount() + 1);
        game.setTimeRemainingInCurrentPhase(game.getGameConfigService().getNightVoteDuration());
        return Phase.NIGHT;
    }
}
