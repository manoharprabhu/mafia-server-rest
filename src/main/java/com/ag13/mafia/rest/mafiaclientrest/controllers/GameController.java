package com.ag13.mafia.rest.mafiaclientrest.controllers;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.*;
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

    @PostMapping(path = "/game/start")
    public HttpResponse<StartGameResponse> startGame(@RequestBody StartGameRequest request) {
        return gameService.startGame(request);
    }

    @PostMapping(path = "/game/vote")
    public HttpResponse<VotePlayerResponse> startGame(@RequestBody VotePlayerRequest request) {
        return gameService.votePlayer(request);
    }

    @PostMapping(path = "/game/police/inspect")
    public HttpResponse<GamePoliceInspectResponse> policeInspect(@RequestBody GamePoliceInspectRequest request) {
        return gameService.policeInspectPlayer(request);
    }
}
