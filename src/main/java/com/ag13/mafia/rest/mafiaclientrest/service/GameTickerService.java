package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GameTickerService {
    private Game game;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private Runnable task;

    public void start(Game game) {
        if(this.game != null) {
            log.info("Game is already running");
            return;
        }
        this.game = game;
        task = this::tick;
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        if(this.game == null) {
            log.info("Game is already stopped");
            return;
        }
        this.game = null;
        this.executor.shutdownNow();
        task = null;
    }

    private void tick() {
        this.game.tick();
    }
}
