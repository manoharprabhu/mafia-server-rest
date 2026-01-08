package com.ag13.mafia.rest.mafiaclientrest.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@Setter
@ToString
public class Player {
    String id;
    String name;
    Role role;
    boolean alive;
    boolean inspectedTonight;

    // Voting
    String votedForPlayerId;          // Day
    String nightTargetPlayerId;       // Mafia/Doctor/Police

    // Role-specific
    String headhunterTargetPlayerId;
    boolean isGodFather;

    // Memory
    ConcurrentLinkedQueue<InspectionResult> inspections;

    ConcurrentLinkedQueue<Message> playerSpecificMessages;

    // Control
    boolean hasActedThisPhase;

    boolean hasVotedToSkipPhase;
}
