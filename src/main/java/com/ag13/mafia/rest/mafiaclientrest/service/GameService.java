package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.GameGetRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.GameGetResponse;
import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.Player;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class GameService {
    @Getter
    private Game currentGame;
    @Getter
    private String currentGameHostPlayerId;

    public void addNewPlayerToExistingGame(String playerId, String playerName) {
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
        currentGame.getPlayers().put(playerId, player);
    }

    public void createNewGameWithplayer(String playerId, String playerName, String lobbyId) {
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
        playersMap.put(playerId, player);

        var game = new Game();
        game.setLobbyId(lobbyId);
        game.setResult(null);
        game.setPlayers(playersMap);
        game.setDayCount(0);
        game.setCurrentPhase(Phase.WAITING_FOR_PLAYERS);
        game.setDaysWithoutVillageKill(0);
        game.setSystemMessages(new ArrayList<>());

        this.currentGame = game;
        this.currentGameHostPlayerId = playerId;
    }
    @SuppressWarnings("unchecked")
    public HttpResponse<GameGetResponse> getState(GameGetRequest request) {
        var lobbyId = request.getLobbyId();
        var playerId = request.getPlayerId();

        // check if the player is in the running game
        if(currentGame == null){
            return (HttpResponse<GameGetResponse>) HttpResponse.createFailureResponse("No game is active right now");
        }

        if(!currentGame.getPlayers().containsKey(playerId)){
            return (HttpResponse<GameGetResponse>) HttpResponse.createFailureResponse("Given player ID is not in the active game");
        }

        if(!Objects.equals(lobbyId, currentGame.getLobbyId())){
            return (HttpResponse<GameGetResponse>) HttpResponse.createFailureResponse("Given lobby ID is not active");
        }

        var player = currentGame.getPlayers().get(playerId);
        var playerRole = player.getRole();

        // construct a base view of the gameboard for the role
        var response = new GameGetResponse();
        response.setPlayers(new ArrayList<>());
        for(Player p : currentGame.getPlayers().values()) {
            response.getPlayers().add(GameGetResponse.Player.create(p));
        }
        response.setGameResult(currentGame.getResult() != null ? currentGame.getResult().name() : null);
        response.setPhase(currentGame.getCurrentPhase());
        response.setMessages(currentGame.getSystemMessages());
        response.setDayNumber(currentGame.getDayCount());
        response.setTimeRemainingSeconds(currentGame.getTimeRemainingInCurrentPhase());

        var you = new GameGetResponse.You();
        you.setAlive(player.isAlive());
        you.setName(player.getName());
        you.setRole(playerRole);
        you.setPlayerId(playerId);
        response.setYou(you);

        HttpResponse<GameGetResponse> gameResponse = new HttpResponse<>();
        gameResponse.setData(response);
        gameResponse.setMessage(null);
        gameResponse.setSuccess(true);
        return gameResponse;
    }
}
