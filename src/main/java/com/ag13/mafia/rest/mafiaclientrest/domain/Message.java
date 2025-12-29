package com.ag13.mafia.rest.mafiaclientrest.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Message {
    private long timestamp;
    private String message;
}
