package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.statemachine.StateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GameTickerService {
    private final ConcurrentLinkedDeque<StateMachine> stateMachine = new ConcurrentLinkedDeque<>();
    private final HashSet<StateMachine> removalSet = new HashSet<>();
    public GameTickerService() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
        executor.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    public void start(StateMachine stateMachine) {
        this.stateMachine.add(stateMachine);
    }

    public void removeStoppedStateMachines() {
        for (StateMachine stateMachine : removalSet) {
            this.stateMachine.remove(stateMachine);
        }
    }

    private void tick() {
        try {
            long startTime = System.nanoTime();
            removeStoppedStateMachines();
            for(StateMachine stateMachine: this.stateMachine) {
                var phase = stateMachine.tick();
                if(phase == Phase.WIN) {
                    removalSet.add(stateMachine);
                }
            }
            long endTime = System.nanoTime();
            log.info("Tick time: {} ms, {} games", (endTime - startTime) / 1_000_000, this.stateMachine.size());
        } catch (Exception ex) {
            log.error("Game ticking failed", ex);
        }
    }
}
