package com.ag13.mafia.rest.mafiaclientrest.DTO.game;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StartGameResponse {
    private String status;

    public static HttpResponse<StartGameResponse> create() {
        var result = new StartGameResponse();
        result.setStatus("success");
        var httpResponse = new HttpResponse<StartGameResponse>();
        httpResponse.setSuccess(true);
        httpResponse.setData(result);
        httpResponse.setMessage(null);
        return httpResponse;
    }
}
