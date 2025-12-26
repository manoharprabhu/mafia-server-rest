package com.ag13.mafia.rest.mafiaclientrest.controllers;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyCreateRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyCreateResponse;
import com.ag13.mafia.rest.mafiaclientrest.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LobbyController {
    @Autowired
    private LobbyService lobbyService;
    @PostMapping(path = "/lobby/create")
    public HttpResponse<LobbyCreateResponse> createLobby(@RequestBody LobbyCreateRequest request) {
        return lobbyService.createLobby(request);
    }
}
