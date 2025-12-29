package com.ag13.mafia.rest.mafiaclientrest;

import com.ag13.mafia.rest.mafiaclientrest.DTO.game.GameGetRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.StartGameRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyCreateRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyCreateResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyJoinRequest;
import com.ag13.mafia.rest.mafiaclientrest.controllers.GameController;
import com.ag13.mafia.rest.mafiaclientrest.controllers.LobbyController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;

@SpringBootTest
class MafiaclientrestApplicationTests {
	@Autowired
	LobbyController lobbyController;
	@Autowired
	GameController gameController;
	@Test
	void contextLoads() {
		// Create a lobby
		LobbyCreateRequest lobbyCreateRequest = new LobbyCreateRequest();
		lobbyCreateRequest.setPlayerName("Manohar");
		var lobbyCreateResponse = lobbyController.createLobby(lobbyCreateRequest);
		Assert.isTrue(lobbyCreateResponse.isSuccess(), "Lobby has been created");

		var lobbyId = lobbyCreateResponse.getData().getLobbyId();
		var creatorId = lobbyCreateResponse.getData().getPlayerId();
		var playerIdList = new ArrayList<String>();
		for(var i = 0; i < 15; i++) {
			var playerJoinRequest = new LobbyJoinRequest();
			playerJoinRequest.setLobbyId(lobbyId);
			playerJoinRequest.setPlayerName(String.valueOf(i));
			var joinResponse = lobbyController.joinLobby(playerJoinRequest);
			Assert.isTrue(joinResponse.isSuccess(), "Lobby has been joined");
			playerIdList.add(joinResponse.getData().getPlayerId());
		}

		var startGameRequest = new StartGameRequest();
		startGameRequest.setLobbyId(lobbyId);
		startGameRequest.setPlayerId(creatorId);

		var startGameResponse = gameController.startGame(startGameRequest);
		Assert.isTrue(startGameResponse.isSuccess(), "Game has been started");

		var gameStateRequest = new GameGetRequest();
		gameStateRequest.setLobbyId(lobbyId);
		gameStateRequest.setPlayerId(creatorId);
		var gameGetResponse = gameController.getState(gameStateRequest);
	}
}
