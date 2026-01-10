package com.ag13.mafia.rest.mafiaclientrest.controllers;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.*;
import com.ag13.mafia.rest.mafiaclientrest.service.LobbyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LobbyController {
    private final LobbyService lobbyService;

    @Autowired
    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }
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

    @PostMapping(path = "/lobby/get")
    public HttpResponse<LobbyGetResponse> joinLobby(@RequestBody LobbyGetRequest request) {
        log.info("Get Lobby");
        var response = lobbyService.getLobby(request);
        log.info(request.toString());
        log.info(response.toString());
        return response;
    }
}
