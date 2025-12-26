package com.ag13.mafia.rest.mafiaclientrest.DTO.lobby;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class LobbyGetResponse {
    List<Player> players;

    public static HttpResponse<LobbyGetResponse> createSuccessResponse(List<Player> players) {
        var lobbyGetResponse = new LobbyGetResponse();
        lobbyGetResponse.setPlayers(players);
        var httpResponse = new HttpResponse<LobbyGetResponse>();
        httpResponse.setSuccess(true);
        httpResponse.setMessage(null);
        httpResponse.setData(lobbyGetResponse);
        return httpResponse;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Player {
        private final String playerId;
        private final String playerName;
    }
}