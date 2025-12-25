package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyCreateRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.LobbyCreateResponse;
import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class LobbyService {
    @Autowired
    private IdGeneratorService idGeneratorService;
    private final int LobbyIDLength = 5;
    private final int PlayerIDLength = 5;

    private Game currentGame;
    private String currentGameHostPlayerId;

    public HttpResponse<LobbyCreateResponse> createLobby(LobbyCreateRequest request) {
        if(currentGame != null) {
            log.error("Lobby has already been created");
            var response = new HttpResponse<LobbyCreateResponse>();
            response.setSuccess(false);
            response.setMessage("Lobby has already been created");
            return response;
        }

        var playerName = request.getPlayerName();
        if(playerName == null || playerName.isEmpty()) {
            log.error("Player name is null or empty");
            var response = new HttpResponse<LobbyCreateResponse>();
            response.setSuccess(false);
            response.setMessage("Player name is null or empty");
            return response;
        }

        var lobbyId = idGeneratorService.getId(LobbyIDLength);
        var playerId = idGeneratorService.getId(PlayerIDLength);

        var player = new Player();
        player.setId(playerId);
        player.setAlive(true);
        player.setName(playerName);
        player.setInspections(new ConcurrentLinkedQueue<>());
        player.setRole(null);
        player.setHeadhunterTargetPlayerId(null);
        player.setNightTargetPlayerId(null);
        player.setHasActedThisPhase(false);
        player.setInspectedTonight(false);
        player.setVotedForPlayerId(null);

        var playersMap = new ConcurrentHashMap<String, Player>();
        playersMap.put(lobbyId, player);

        var game = new Game();
        game.setLobbyId(lobbyId);
        game.setResult(null);
        game.setPlayers(playersMap);
        game.setDayCount(0);
        game.setCurrentPhase(Phase.WAITING_FOR_PLAYERS);
        game.setDaysWithoutVillageKill(0);

        this.currentGame = game;
        this.currentGameHostPlayerId = playerId;

        return LobbyCreateResponse.createSuccessResponse(playerId);
    }
}
