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
}
