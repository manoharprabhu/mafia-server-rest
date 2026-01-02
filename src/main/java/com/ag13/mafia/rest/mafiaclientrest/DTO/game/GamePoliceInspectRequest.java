package com.ag13.mafia.rest.mafiaclientrest.DTO.game;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GamePoliceInspectRequest {
    private String lobbyId;
    private String playerId;
    private String targetPlayerId;
}
