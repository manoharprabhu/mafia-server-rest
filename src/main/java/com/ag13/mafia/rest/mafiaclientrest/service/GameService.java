package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.*;
import com.ag13.mafia.rest.mafiaclientrest.domain.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
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

    @SuppressWarnings("unchecked")
    public HttpResponse<VotePlayerResponse> votePlayer(VotePlayerRequest request) {
        var lobbyId = request.getLobbyId();
        var playerId = request.getPlayerId();
        var targetPlayerId = request.getTargetPlayerId();
        var voteType = request.getType();
        // check if the player is in the running game
        if(currentGame == null){
            return (HttpResponse<VotePlayerResponse>) HttpResponse.createFailureResponse("No game is active right now");
        }

        if(!currentGame.getPlayers().containsKey(playerId)){
            return (HttpResponse<VotePlayerResponse>) HttpResponse.createFailureResponse("Given player ID is not in the active game");
        }

        if(!currentGame.getPlayers().containsKey(targetPlayerId)){
            return (HttpResponse<VotePlayerResponse>) HttpResponse.createFailureResponse("Given target player ID is not in the active game");
        }

        if(!Objects.equals(lobbyId, currentGame.getLobbyId())){
            return (HttpResponse<VotePlayerResponse>) HttpResponse.createFailureResponse("Given lobby ID is not active");
        }

        if(!Objects.equals(voteType, "villager") && !Objects.equals(voteType, "mafia")){
            return (HttpResponse<VotePlayerResponse>) HttpResponse.createFailureResponse("vote type must be either villager or mafia");
        }

        var success = false;
        switch (voteType){
            case "villager":
                success = handleVillagerVote(playerId, targetPlayerId);
                break;
            case "mafia":
                success = handleMafiaVote(playerId, targetPlayerId);
                break;
        }

        return VotePlayerResponse.create(success);
    }

    private boolean handleMafiaVote(String playerId, String targetPlayerId) {
        if(!currentGame.getCurrentPhase().equals(Phase.NIGHT)) {
            log.info("Mafia can only vote during NIGHT phase");
            return false;
        }
        if(Objects.equals(targetPlayerId, playerId)) {
            log.info("Player cannot vote for self");
            return false;
        }
        if(!currentGame.getPlayers().get(playerId).isAlive() || !currentGame.getPlayers().get(targetPlayerId).isAlive()) {
            log.info("source and target player must be both alive to vote");
            return false;
        }

        if(currentGame.getPlayers().get(playerId).getRole() != Role.MAFIA) {
            log.info("Only mafia can vote at night");
            return false;
        }

        if(currentGame.getPlayers().get(targetPlayerId).getRole() == Role.MAFIA) {
            log.info("Player can only vote for mafia");
            return false;
        }

        currentGame.getPlayers().get(playerId).setNightTargetPlayerId(targetPlayerId);
        return true;
    }

    private boolean handleVillagerVote(String playerId, String targetPlayerId) {
        if(!currentGame.getCurrentPhase().equals(Phase.DAY_VOTING)) {
            log.info("Villager can only vote during DAY_VOTING phase");
            return false;
        }

        if(Objects.equals(targetPlayerId, playerId)) {
            log.info("Player cannot vote for self");
            return false;
        }

        if(!currentGame.getPlayers().get(playerId).isAlive() || !currentGame.getPlayers().get(targetPlayerId).isAlive()) {
            log.info("source and target player must be both alive to vote");
            return false;
        }

        currentGame.getPlayers().get(playerId).setVotedForPlayerId(targetPlayerId);
        return true;
    }
}
