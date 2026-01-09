package com.ag13.mafia.rest.mafiaclientrest.domain.statemachine;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;

import java.util.HashMap;
import java.util.Map;

public class StateMachine {
    Map<Phase, IState> states;
    private IState currentState;
    private final Game game;

    public StateMachine(Game game) {
        states = new HashMap<>();
        states.put(Phase.START, new StartState(game));
        states.put(Phase.NIGHT, new NightState(game));
        states.put(Phase.RESOLVE_NIGHT, new ResolveNightState(game));
        states.put(Phase.DAY_DISCUSSION, new DayDiscussionPhase(game));
        states.put(Phase.DAY_VOTING, new DayVotingState(game));
        states.put(Phase.RESOLVE_DAY, new ResolveDayState(game));
        states.put(Phase.WIN, new WinPhase(game));
        this.game = game;
        currentState = states.get(Phase.START);
    }

    public void tick() {
        if(currentState != null) {
            var nextPhase = currentState.execute();
            game.setCurrentPhase(nextPhase);
            currentState = states.get(nextPhase);
        }
    }
}
