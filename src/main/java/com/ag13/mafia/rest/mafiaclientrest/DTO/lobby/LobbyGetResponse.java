package com.ag13.mafia.rest.mafiaclientrest.DTO.lobby;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
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
    String lobbyCreatorId;
    Phase currentPhase;

    public static HttpResponse<LobbyGetResponse> createSuccessResponse(List<Player> players, String hostPlayerId, Phase currentPhase) {
        var lobbyGetResponse = new LobbyGetResponse();
        lobbyGetResponse.setPlayers(players);
        lobbyGetResponse.setLobbyCreatorId(hostPlayerId);
        lobbyGetResponse.setCurrentPhase(currentPhase);
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