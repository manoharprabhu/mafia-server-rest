package com.ag13.mafia.rest.mafiaclientrest.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HttpResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static HttpResponse<?> createFailureResponse(String message) {
        HttpResponse<Object> response = new HttpResponse<>();
        response.setData(null);
        response.setMessage(message);
        return response;
    }
}
