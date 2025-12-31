package com.ag13.mafia.rest.mafiaclientrest.DTO.game;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VotePlayerResponse {
    private boolean success;

    public static HttpResponse<VotePlayerResponse> create(boolean success) {
        var result = new VotePlayerResponse();
        result.setSuccess(success);
        var httpResponse = new HttpResponse<VotePlayerResponse>();
        httpResponse.setSuccess(true);
        httpResponse.setData(result);
        httpResponse.setMessage(null);
        return httpResponse;
    }
}
