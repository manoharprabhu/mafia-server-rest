package com.ag13.mafia.rest.mafiaclientrest.domain.statemachine;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;

public class DayVotingState implements IState {
    private final Game game;

    public DayVotingState(Game game) {
        this.game = game;
    }

    @Override
    public Phase execute() {
        if(game.getTimeRemainingInCurrentPhase() > 0) {
            game.setTimeRemainingInCurrentPhase(game.getTimeRemainingInCurrentPhase() - 1);
            return Phase.DAY_VOTING;
        }
        return Phase.RESOLVE_DAY;
    }
}
