package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.GameGetRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.GameGetResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.StartGameRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.StartGameResponse;
import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Message;
import com.ag13.mafia.rest.mafiaclientrest.domain.Phase;
import com.ag13.mafia.rest.mafiaclientrest.domain.Player;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    GameTickerService gameTickerService;

    @Autowired
    GameConfigService gameConfigService;

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
        player.setPlayerSpecificMessages(new ConcurrentLinkedQueue<>());
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
        player.setPlayerSpecificMessages(new ConcurrentLinkedQueue<>());

        var playersMap = new ConcurrentHashMap<String, Player>();
        playersMap.put(playerId, player);

        var game = new Game();
        game.setLobbyId(lobbyId);
        game.setResult(null);
        game.setPlayers(playersMap);
        game.setDayCount(0);
        game.setCurrentPhase(Phase.WAITING_FOR_PLAYERS);
        game.setDaysWithoutVillageKill(0);
        game.setAllPlayerMessages(new ArrayList<>());
        // todo set this from the Player object
        //game.setPlayerSpecificMessages(new ConcurrentHashMap<>());

        this.currentGame = game;
        this.currentGame.setGameConfigService(gameConfigService);
        this.currentGameHostPlayerId = playerId;
    }

    @SuppressWarnings("unchecked")
    public HttpResponse<StartGameResponse> startGame(StartGameRequest request) {
        var lobbyId = request.getLobbyId();
        var playerId = request.getPlayerId();
        if(currentGame == null){
            return (HttpResponse<StartGameResponse>) HttpResponse.createFailureResponse("No game is active right now");
        }

        if(!currentGame.getPlayers().containsKey(playerId)){
            return (HttpResponse<StartGameResponse>) HttpResponse.createFailureResponse("Given player ID is not in the active game");
        }

        if(!Objects.equals(lobbyId, currentGame.getLobbyId())){
            return (HttpResponse<StartGameResponse>) HttpResponse.createFailureResponse("Given lobby ID is not active");
        }

        this.currentGame.setCurrentPhase(Phase.START);
        gameTickerService.start(currentGame);

        return StartGameResponse.create();
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

        response.setMessages(new ArrayList<>());
        for(Message m : currentGame.getAllPlayerMessages()) {
            response.getMessages().add(GameGetResponse.Message.create(m));
        }

        // todo - set player specific messages from the Player object here
//        response.setPlayerSpecificMessages(new ConcurrentHashMap<>());
//        for(String key : currentGame.getPlayerSpecificMessages().keySet()) {
//            var value = currentGame.getPlayerSpecificMessages().get(key);
//            var messages = GameGetResponse.Message.create(value);
//            response.getPlayerSpecificMessages().put(key, messages);
//        }

        response.setVisibleRoles(new ConcurrentHashMap<>());

        response.setDayNumber(currentGame.getDayCount());
        response.setTimeRemainingSeconds(currentGame.getTimeRemainingInCurrentPhase());
        response.setVoteMap(new ConcurrentHashMap<>());

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
