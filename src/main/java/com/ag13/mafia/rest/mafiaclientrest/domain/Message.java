package com.ag13.mafia.rest.mafiaclientrest.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Message {
    private final long timestamp;
    private final String message;
}
