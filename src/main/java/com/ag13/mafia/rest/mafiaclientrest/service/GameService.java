package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.GameGetRequest;
import com.ag13.mafia.rest.mafiaclientrest.DTO.lobby.GameGetResponse;
import com.ag13.mafia.rest.mafiaclientrest.domain.Game;
import com.ag13.mafia.rest.mafiaclientrest.domain.Player;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class GameService {
    @Getter
    @Setter
    private Game currentGame;

    @Getter
    @Setter
    private String currentGameHostPlayerId;

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
