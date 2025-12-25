package com.ag13.mafia.rest.mafiaclientrest.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Game {
    String lobbyId;
    ConcurrentHashMap<String, Player> players;
    Phase currentPhase;
    int dayCount;
    int daysWithoutVillageKill;
    GameResult result;
}
