package com.ag13.mafia.rest.mafiaclientrest.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class GameConfigService {
    private final int dayDuration = 10;
    private final int dayVoteDuration = 10;
    private final int nightVoteDuration = 10;
    private final int minPlayers = 10;
    private final int maxPlayers = 16;
    private final int lobbyIDLength = 3;
    private final int playerIDLength = 3;
    private final boolean isHeadhunterEnabled = true;
    private final boolean isFoolEnabled = false;
    private final int maxUserMessageLength = 200;
}
