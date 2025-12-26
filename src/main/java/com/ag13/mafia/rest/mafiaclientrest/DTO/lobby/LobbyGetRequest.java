package com.ag13.mafia.rest.mafiaclientrest.DTO.lobby;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LobbyGetRequest {
    private String lobbyId;
    private String playerId;
}
