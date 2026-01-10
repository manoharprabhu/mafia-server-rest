package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.*;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class LobbyService {
    private final IdGeneratorService idGeneratorService;
    private final GameService gameService;
    private final GameConfigService gameConfigService;

    @Autowired
    public LobbyService(GameConfigService gameConfigService, GameService gameService, IdGeneratorService idGeneratorService) {
        this.gameConfigService = gameConfigService;
        this.gameService = gameService;
        this.idGeneratorService = idGeneratorService;
    }

    public HttpResponse<LobbyCreateResponse> createLobby(LobbyCreateRequest request) {
        if(gameService.getCurrentGame() != null) {
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

        var lobbyId = idGeneratorService.getId(gameConfigService.getLobbyIDLength());
        var playerId = idGeneratorService.getId(gameConfigService.getPlayerIDLength());
        gameService.createNewGameWithplayer(playerId, playerName, lobbyId);

        return LobbyCreateResponse.createSuccessResponse(playerId, lobbyId);
    }

    public HttpResponse<LobbyJoinResponse> joinLobby(LobbyJoinRequest request) {
        var currentGame = gameService.getCurrentGame();
        if(currentGame == null) {
            log.error("There is no active game to join");
            var response = new HttpResponse<LobbyJoinResponse>();
            response.setSuccess(false);
            response.setMessage("There is no active game to join");
            return response;
        }

        var playerName = request.getPlayerName();
        var lobbyId = request.getLobbyId();

        if(playerName == null || playerName.isEmpty()) {
            log.error("Player name is null or empty");
            var response = new HttpResponse<LobbyJoinResponse>();
            response.setSuccess(false);
            response.setMessage("Player name is null or empty");
            return response;
        }

        if(lobbyId == null || lobbyId.isEmpty()) {
            log.error("Lobby id is null or empty");
            var response = new HttpResponse<LobbyJoinResponse>();
            response.setSuccess(false);
            response.setMessage("Lobby id is null or empty");
            return response;
        }

        if(!currentGame.getLobbyId().equals(lobbyId)) {
            log.error("Lobby ID " + lobbyId + " does not exist");
            var response = new HttpResponse<LobbyJoinResponse>();
            response.setSuccess(false);
            response.setMessage("Lobby ID " + lobbyId + " does not exist");
            return response;
        }

        if(!currentGame.getCurrentPhase().equals(Phase.WAITING_FOR_PLAYERS)) {
            log.error("Lobby is not accepting new players.");
            var response = new HttpResponse<LobbyJoinResponse>();
            response.setSuccess(false);
            response.setMessage("Lobby is not accepting new players");
            return response;
        }

        if(currentGame.getPlayers().size() >= gameConfigService.getMaxPlayers()) {
            log.error("Lobby is at full capacity. Cannot join");
            var response = new HttpResponse<LobbyJoinResponse>();
            response.setSuccess(false);
            response.setMessage("Lobby is at full capacity");
            return response;
        }

        var playerId = idGeneratorService.getId(gameConfigService.getPlayerIDLength());
        gameService.addNewPlayerToExistingGame(playerId, playerName);

        return LobbyJoinResponse.createSuccessResponse(playerId);
    }

    public HttpResponse<LobbyGetResponse> getLobby(LobbyGetRequest request) {
        var currentGame = gameService.getCurrentGame();
        if(currentGame == null) {
            log.error("There is no active game to get");
            var response = new HttpResponse<LobbyGetResponse>();
            response.setSuccess(false);
            response.setMessage("There is no active game to get");
            return response;
        }

        var lobbyId = request.getLobbyId();
        if(lobbyId == null || lobbyId.isEmpty()) {
            log.error("Lobby id is null or empty");
            var response = new HttpResponse<LobbyGetResponse>();
            response.setSuccess(false);
            response.setMessage("Lobby id is null or empty");
            return response;
        }

        var playerId = request.getPlayerId();
        if(playerId == null || playerId.isEmpty()) {
            log.error("Player id is null or empty");
            var response = new HttpResponse<LobbyGetResponse>();
            response.setSuccess(false);
            response.setMessage("Player id is null or empty");
            return response;
        }

        if(!currentGame.getLobbyId().equals(lobbyId)) {
            log.error("Wrong lobby Id");
            var response = new HttpResponse<LobbyGetResponse>();
            response.setSuccess(false);
            response.setMessage("Wrong lobby Id");
            return response;
        }

        if(!currentGame.getPlayers().containsKey(playerId)) {
            log.error("Player " + playerId + " cannot get the lobby because they have not joined it");
            var response = new HttpResponse<LobbyGetResponse>();
            response.setSuccess(false);
            response.setMessage("Player " + playerId + " cannot get the lobby");
            return response;
        }

        var playersList = new ArrayList<LobbyGetResponse.Player>();
        var allPlayers = currentGame.getPlayers();
        for(var key : allPlayers.keySet()) {
            playersList.add(new LobbyGetResponse.Player(key, allPlayers.get(key).getName()));
        }
        return LobbyGetResponse.createSuccessResponse(playersList, gameService.getCurrentGameHostPlayerId(), currentGame.getCurrentPhase());
    }
}