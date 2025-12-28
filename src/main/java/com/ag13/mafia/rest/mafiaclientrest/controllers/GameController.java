package com.ag13.mafia.rest.mafiaclientrest.controllers;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.GameGetRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.GameGetResponse;
import com.ag13.mafia.rest.mafiaclientrest.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class GameController {
    @Autowired
    private GameService gameService;
    @PostMapping(path = "/game/state")
    public HttpResponse<GameGetResponse> getState(@RequestBody GameGetRequest request) {
        return gameService.getState(request);
    }
}
