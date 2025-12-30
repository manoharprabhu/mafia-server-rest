package com.ag13.mafia.rest.mafiaclientrest.controllers;

import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyJoinRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    LobbyController lobbyController;
    @Autowired
    GameController gameController;

    @GetMapping(path = "/adddummyplayers")
    public String joinLobby(@RequestParam String lobbyId, @RequestParam int size) {
        var successCount = 0;
        var failureCount = 0;
        for(var i = 0; i < size; i++) {
            var playerJoinRequest = new LobbyJoinRequest();
            playerJoinRequest.setLobbyId(lobbyId);
            playerJoinRequest.setPlayerName(String.valueOf(i));
            var response = lobbyController.joinLobby(playerJoinRequest);
            if(response.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }
        return "Success:" + successCount + " - " + " Failure:" + failureCount;
    }
}
