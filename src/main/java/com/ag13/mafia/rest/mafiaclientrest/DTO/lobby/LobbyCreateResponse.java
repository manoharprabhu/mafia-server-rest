package com.ag13.mafia.rest.mafiaclientrest.DTO.lobby;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import lombok.Getter;

public class LobbyCreateResponse {
    @Getter
    private String playerId;

    public static HttpResponse<LobbyCreateResponse> createSuccessResponse(String playerId) {
        var lobbyResponse = new LobbyCreateResponse();
        lobbyResponse.playerId = playerId;
        var httpResponse = new HttpResponse<LobbyCreateResponse>();
        httpResponse.setSuccess(true);
        httpResponse.setMessage(null);
        httpResponse.setData(lobbyResponse);
        return httpResponse;
    }
}
