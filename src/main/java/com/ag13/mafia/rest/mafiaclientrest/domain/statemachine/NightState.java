package com.ag13.mafia.rest.mafiaclientrest.domain.statemachine;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;

public class NightState implements IState {
    private final Game game;

    public NightState(Game game) {
        this.game = game;
    }

    @Override
    public Phase execute() {
        if(game.getTimeRemainingInCurrentPhase() > 0) {
            game.setTimeRemainingInCurrentPhase(game.getTimeRemainingInCurrentPhase() - 1);
            return Phase.NIGHT;
        }
        return Phase.RESOLVE_NIGHT;
    }
}
