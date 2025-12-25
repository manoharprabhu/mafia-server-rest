package com.ag13.mafia.rest.mafiaclientrest.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
