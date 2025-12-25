package com.ag13.mafia.rest.mafiaclientrest.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@Setter
public class Player {
    String id;
    String name;
    Role role;
    boolean alive;
    boolean protectedTonight;
    boolean inspectedTonight;

    // Voting
    String votedForPlayerId;          // Day
    String nightTargetPlayerId;       // Mafia/Doctor/Police

    // Role-specific
    String headhunterTargetPlayerId;

    // Memory
    ConcurrentLinkedQueue<InspectionResult> inspections;

    // Control
    boolean hasActedThisPhase;
}
