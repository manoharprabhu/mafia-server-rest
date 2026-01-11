package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.statemachine.StateMachine;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Service
@Slf4j
public class GameTickerService {
    private final ConcurrentLinkedDeque<StateMachine> stateMachine = new ConcurrentLinkedDeque<>();
    private final Set<StateMachine> removalSet = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler;
    private final ExecutorService workerPool;
    private final int batchSize;
    
    public GameTickerService(
            @Value("${game.ticker.batch-size:8}") int batchSize,
            @Value("${game.ticker.max-threads:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}") int maxThreads) {
        this.batchSize = batchSize;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.workerPool = Executors.newFixedThreadPool(maxThreads);
        scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
        log.info("GameTickerService initialized with batch-size={}, max-threads={}", batchSize, maxThreads);
    }

    public void start(StateMachine stateMachine) {
        this.stateMachine.add(stateMachine);
    }

    public void removeStoppedStateMachines() {
        for (StateMachine stateMachine : removalSet) {
            this.stateMachine.remove(stateMachine);
        }
        removalSet.clear();
    }

    private void tick() {
        try {
            long startTime = System.nanoTime();
            removeStoppedStateMachines();
            
            List<StateMachine> machines = new ArrayList<>(this.stateMachine);
            if (machines.isEmpty()) {
                return;
            }
            
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (StateMachine machine : machines) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        var phase = machine.tick();
                        if (phase == Phase.WIN) {
                            removalSet.add(machine);
                        }
                    } catch (Exception ex) {
                        log.error("Failed to tick game state machine", ex);
                    }
                }, workerPool);
                futures.add(future);
                
                // Wait for batch to complete before starting next batch
                if (futures.size() >= batchSize) {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    futures.clear();
                }
            }

            if (!futures.isEmpty()) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
            
            long endTime = System.nanoTime();
            log.info("Tick time: {} ms, {} games", (endTime - startTime) / 1_000_000, machines.size());
        } catch (Exception ex) {
            log.error("Game ticking failed", ex);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down GameTickerService");
        scheduler.shutdown();
        workerPool.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            scheduler.shutdownNow();
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
