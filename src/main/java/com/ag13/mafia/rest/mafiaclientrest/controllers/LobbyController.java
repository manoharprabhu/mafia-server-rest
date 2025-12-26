package com.ag13.mafia.rest.mafiaclientrest.controllers;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyCreateRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyCreateResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyJoinRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyJoinResponse;
import com.ag13.mafia.rest.mafiaclientrest.service.LobbyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class LobbyController {
    @Autowired
    private LobbyService lobbyService;
    @PostMapping(path = "/lobby/create")
    public HttpResponse<LobbyCreateResponse> createLobby(@RequestBody LobbyCreateRequest request) {
        log.info("Create Lobby");
        var response = lobbyService.createLobby(request);
        log.info(request.toString());
        log.info(response.toString());
        return response;
    }

    @PostMapping(path = "/lobby/join")
    public HttpResponse<LobbyJoinResponse> joinLobby(@RequestBody LobbyJoinRequest request) {
        log.info("Join Lobby");
        var response = lobbyService.joinLobby(request);
        log.info(request.toString());
        log.info(response.toString());
        return response;
    }
}
