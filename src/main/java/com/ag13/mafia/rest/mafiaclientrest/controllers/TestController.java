package com.ag13.mafia.rest.mafiaclientrest.controllers;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.GameGetRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.StartGameRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyCreateRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyJoinRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyJoinResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class TestController {
    @Autowired
    LobbyController lobbyController;
    @Autowired
    GameController gameController;

    @GetMapping(path = "/test")
    public void joinLobby() {
        // Create a lobby
        LobbyCreateRequest lobbyCreateRequest = new LobbyCreateRequest();
        lobbyCreateRequest.setPlayerName("Manohar");
        var lobbyCreateResponse = lobbyController.createLobby(lobbyCreateRequest);

        var lobbyId = lobbyCreateResponse.getData().getLobbyId();
        var creatorId = lobbyCreateResponse.getData().getPlayerId();
        var playerIdList = new ArrayList<String>();
        for(var i = 0; i < 14; i++) {
            var playerJoinRequest = new LobbyJoinRequest();
            playerJoinRequest.setLobbyId(lobbyId);
            playerJoinRequest.setPlayerName(String.valueOf(i));
            var joinResponse = lobbyController.joinLobby(playerJoinRequest);
            playerIdList.add(joinResponse.getData().getPlayerId());
        }
    }
}
