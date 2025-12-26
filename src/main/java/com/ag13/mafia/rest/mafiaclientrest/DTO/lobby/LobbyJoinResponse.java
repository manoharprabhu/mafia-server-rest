package com.ag13.mafia.rest.mafiaclientrest.DTO.lobby;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import lombok.Getter;
import lombok.ToString;

@ToString
public class LobbyJoinResponse {
    @Getter
    private String playerId;

    public static HttpResponse<LobbyJoinResponse> createSuccessResponse(String playerId) {
        var lobbyResponse = new LobbyJoinResponse();
        lobbyResponse.playerId = playerId;
        var httpResponse = new HttpResponse<LobbyJoinResponse>();
        httpResponse.setSuccess(true);
        httpResponse.setMessage(null);
        httpResponse.setData(lobbyResponse);
        return httpResponse;
    }
}
