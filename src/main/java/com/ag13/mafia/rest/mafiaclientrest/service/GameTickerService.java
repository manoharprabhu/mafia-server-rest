package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.statemachine.StateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.plaf.nimbus.State;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GameTickerService {
    private StateMachine stateMachine;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
    private Runnable task;

    public void start(StateMachine stateMachine) {
        if(this.stateMachine != null) {
            log.info("Game is already running");
            return;
        }
        this.stateMachine = stateMachine;
        task = this::tick;
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        if(this.stateMachine == null) {
            log.info("Game is already stopped");
            return;
        }
        this.stateMachine = null;
        this.executor.shutdownNow();
        task = null;
    }

    private void tick() {
        try {
            var phase = this.stateMachine.tick();
            if(phase == Phase.WIN) {
                log.info("Game has ended. stopping the ticker");
                stop();
            }
        } catch (Exception ex) {
            log.error("Game ticking failed", ex);
        }
    }
}
