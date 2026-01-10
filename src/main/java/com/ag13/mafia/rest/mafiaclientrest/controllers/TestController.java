package com.ag13.mafia.rest.mafiaclientrest.controllers;

import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyJoinRequest;
import com.ag13.mafia.rest.mafiaclientrest.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
public class TestController {
    private final LobbyController lobbyController;
    private final GameService gameService;

    @Autowired
    public TestController(LobbyController lobbyController, GameService gameService) {
        this.lobbyController = lobbyController;
        this.gameService = gameService;
    }

    private String[] playerNames = new  String[]{
            "Tejram",
            "Harish",
            "Naman",
            "Parul",
            "Nikhil",
            "Saurabh",
            "Sandeep",
            "Rohit",
            "Priya",
            "Keshav",
            "Shravan",
            "Subal"
    };

    @GetMapping(path = "/adddummyplayers")
    public String joinLobby(@RequestParam String lobbyId, @RequestParam int size) {
        var successCount = 0;
        var failureCount = 0;
        List<String> list = Arrays.asList(playerNames);
        Collections.shuffle(list);
        for(var i = 0; i < size; i++) {
            var playerJoinRequest = new LobbyJoinRequest();
            playerJoinRequest.setLobbyId(lobbyId);
            playerJoinRequest.setPlayerName(list.get(i % list.size()));
            var response = lobbyController.joinLobby(playerJoinRequest);
            if(response.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }
        return "Success:" + successCount + " - " + " Failure:" + failureCount;
    }

    @GetMapping(path = "/skip")
    public void skip(@RequestParam int size, @RequestParam String lobbyId) {
        gameService.skip(lobbyId, size);
    }

    @GetMapping(path = "/kill")
    public void kill(@RequestParam String playerId, @RequestParam String lobbyId) {
        gameService.kill(lobbyId, playerId);
    }

    @GetMapping(path = "/randomvote")
    public void randomVote(@RequestParam String lobbyId) {
        gameService.randomVote(lobbyId);
    }
}
