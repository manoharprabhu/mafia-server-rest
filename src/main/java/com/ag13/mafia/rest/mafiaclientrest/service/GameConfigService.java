package com.ag13.mafia.rest.mafiaclientrest.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class GameConfigService {
    private final int dayDuration = 120;
    private final int dayVoteDuration = 60;
    private final int nightVoteDuration = 30;
    private final int minPlayers = 10;
    private final int maxPlayers = 16;
}
