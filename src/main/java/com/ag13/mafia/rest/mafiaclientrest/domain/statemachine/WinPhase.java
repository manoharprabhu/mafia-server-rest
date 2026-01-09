package com.ag13.mafia.rest.mafiaclientrest.domain.statemachine;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;

public class WinPhase implements IState {
    private final Game game;

    public WinPhase(Game game) {
        this.game = game;
    }

    @Override
    public Phase execute() {
        return Phase.WIN;
    }
}
