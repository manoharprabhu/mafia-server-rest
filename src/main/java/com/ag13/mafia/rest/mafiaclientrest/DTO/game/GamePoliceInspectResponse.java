package com.ag13.mafia.rest.mafiaclientrest.DTO.game;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GamePoliceInspectResponse {
    private boolean success;

    public static HttpResponse<GamePoliceInspectResponse> create(boolean success) {
        var result = new GamePoliceInspectResponse();
        result.setSuccess(success);
        var httpResponse = new HttpResponse<GamePoliceInspectResponse>();
        httpResponse.setSuccess(true);
        httpResponse.setData(result);
        httpResponse.setMessage(null);
        return httpResponse;
    }
}
