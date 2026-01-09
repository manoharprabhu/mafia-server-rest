package com.ag13.mafia.rest.mafiaclientrest.domain.statemachine;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.Player;

public class DayDiscussionPhase implements IState{
    private final Game game;

    public DayDiscussionPhase(Game game) {
        this.game = game;
    }

    @Override
    public Phase execute() {
        var numberOfPlayersAlive = game.getNumberOfPlayersAlive();
        if(eligibleForPhaseSkip()) {
            game.setTimeRemainingInCurrentPhase(game.getGameConfigService().getDayVoteDuration());
            game.addAllPlayerMessage("Majority has voted to skip the discussion");
            game.addAllPlayerMessage("Get ready to vote. (Alteast " + ((int)Math.floor((double) numberOfPlayersAlive / 2)) + " votes required)");
            return Phase.DAY_VOTING;
        }

        if(game.getTimeRemainingInCurrentPhase() > 0) {
            game.setTimeRemainingInCurrentPhase(game.getTimeRemainingInCurrentPhase() - 1);
            return Phase.DAY_DISCUSSION;
        }


        game.setTimeRemainingInCurrentPhase(game.getGameConfigService().getDayVoteDuration());
        game.addAllPlayerMessage("Get ready to vote. (Alteast " + ((int)Math.floor((double) numberOfPlayersAlive / 2)) + " votes required)");
        return Phase.DAY_VOTING;
    }

    private boolean eligibleForPhaseSkip() {
        var skipVotes = 0;
        for(Player player : game.getPlayers().values()) {
            skipVotes += player.isHasVotedToSkipPhase() ? 1 : 0;
        }

        var alivePlayersCount = game.getNumberOfPlayersAlive();
        return skipVotes >= Math.ceil((double) alivePlayersCount / 2);
    }
}
