package com.ag13.mafia.rest.mafiaclientrest.DTO.game;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VoteSkipRequest {
    private String lobbyId;
    private String playerId;
}
