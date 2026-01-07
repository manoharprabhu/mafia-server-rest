package com.ag13.mafia.rest.mafiaclientrest.service;

import com.ag13.mafia.rest.mafiaclientrest.DTO.HttpResponse;
import com.ag13.mafia.rest.mafiaclientrest.DTO.game.*;
import com.ag13.mafia.rest.mafiaclientrest.domain.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    final GameTickerService gameTickerService;

    final GameConfigService gameConfigService;

    public GameService(GameTickerService gameTickerService, GameConfigService gameConfigService) {
        this.gameTickerService = gameTickerService;
        this.gameConfigService = gameConfigService;
    }

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
        game.setDayCount(1);
        game.setCurrentPhase(Phase.WAITING_FOR_PLAYERS);
        game.setDaysWithoutVillageKill(0);
        game.setAllPlayerMessages(new ConcurrentLinkedQueue<>());
        game.setResult(GameResult.NONE);
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

        ConcurrentHashMap<String, Role> visibleRoles = getVisibleRolesForPlayer(playerId);
        response.setVisibleRoles(visibleRoles);


        response.setDayNumber(currentGame.getDayCount());
        response.setTimeRemainingSeconds(currentGame.getTimeRemainingInCurrentPhase());

        ConcurrentHashMap<String, String> voteMap = getVoteMap(playerId);
        response.setVoteMap(voteMap);

        List<InspectionResult> inspectionResults = getInspectionResults(playerId);
        response.setInspectionResults(inspectionResults);

        response.setYourHeadhunterTarget(getMyHeadhunterTarget(playerId));
        response.setWinner(getWinner());

        response.setHasInspectedAlready(hasPoliceAlreadyInspected(playerId));
        response.setNumberOfPlayersSkipDiscussion(getNumberOfPlayersWhoSkippedDiscussion());

        var you = new GameGetResponse.You();
        you.setAlive(player.isAlive());
        you.setName(player.getName());
        you.setRole(playerRole);
        you.setPlayerId(playerId);
        you.setHasSkippedDiscussion(player.isHasVotedToSkipPhase());
        response.setYou(you);

        HttpResponse<GameGetResponse> gameResponse = new HttpResponse<>();
        gameResponse.setData(response);
        gameResponse.setMessage(null);
        gameResponse.setSuccess(true);
        return gameResponse;
    }

    private int getNumberOfPlayersWhoSkippedDiscussion() {
        var count = 0;
        for(Player player : currentGame.getPlayers().values()){
            count += player.isHasVotedToSkipPhase() ? 1 : 0;
        }

        return count;
    }

    private boolean hasPoliceAlreadyInspected(String playerId) {
        var player = currentGame.getPlayers().get(playerId);
        if(player.getRole() == Role.POLICE){
            return player.isInspectedTonight();
        } else {
            return false;
        }
    }

    private GameResult getWinner() {
        return currentGame.getResult();
    }

    private String getMyHeadhunterTarget(String playerId) {
        var player = currentGame.getPlayers().get(playerId);
        if(player.getRole() != Role.HEADHUNTER) {
            return null;
        }

        return player.getHeadhunterTargetPlayerId();
    }

    private List<InspectionResult> getInspectionResults(String playerId) {
        var result = new ArrayList<InspectionResult>();
        if(currentGame.getPlayers().get(playerId).getRole() != Role.POLICE){
            return result;
        }

        return new ArrayList<>(currentGame.getPlayers().get(playerId).getInspections());
    }

    private ConcurrentHashMap<String, String> getVoteMap(String playerId) {
        // show mafia votes only to mafia players at night
        // show everyone's votes to everyone at day
        // show doctor vote only to doctor
        ConcurrentHashMap<String, String> voteMap = new ConcurrentHashMap<>();
        if(currentGame.getPlayers().get(playerId).getRole() == Role.MAFIA && currentGame.getCurrentPhase() == Phase.NIGHT) {
            for(Player p : currentGame.getPlayers().values()) {
                if(p.getRole() == Role.MAFIA && p.getNightTargetPlayerId() != null) {
                    voteMap.put(p.getId(), p.getNightTargetPlayerId());
                }
            }
        } else if(currentGame.getPlayers().get(playerId).getRole() == Role.DOCTOR && currentGame.getCurrentPhase() == Phase.NIGHT) {
            if(currentGame.getPlayers().get(playerId).getNightTargetPlayerId() != null) {
                voteMap.put(playerId, currentGame.getPlayers().get(playerId).getNightTargetPlayerId());
            }
        } else if(currentGame.getCurrentPhase() == Phase.DAY_VOTING) {
            for(Player p : currentGame.getPlayers().values()) {
                if(p.getVotedForPlayerId() != null) {
                    voteMap.put(p.getId(), p.getVotedForPlayerId());
                }
            }
        }

        return voteMap;
    }

    private ConcurrentHashMap<String, Role> getVisibleRolesForPlayer(String playerId) {
        var result = new ConcurrentHashMap<String, Role>();
        if(currentGame.getCurrentPhase() == Phase.WIN) {
            for(Player p : currentGame.getPlayers().values()){
                result.put(p.getId(), p.getRole());
            }
            return result;
        }

        if(currentGame.getPlayers().get(playerId).getRole() == Role.MAFIA) {
            // show other mafias
            for(Player p : currentGame.getPlayers().values()){
                if(p.getRole() == Role.MAFIA || !p.isAlive()) {
                    result.put(p.getId(), p.getRole());
                }
            }
            return result;
        }

        // return dead people's role by default
        for(Player p : currentGame.getPlayers().values()){
            if(!p.isAlive()) {
                result.put(p.getId(), p.getRole());
            }
        }
        return result;
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

        if(!Objects.equals(voteType, "villager") && !Objects.equals(voteType, "mafia") && !Objects.equals(voteType, "doctor")) {
            return (HttpResponse<VotePlayerResponse>) HttpResponse.createFailureResponse("vote type must be either villager or mafia or doctor");
        }

        var success = false;
        switch (voteType){
            case "villager":
                success = handleVillagerVote(playerId, targetPlayerId);
                break;
            case "mafia":
                success = handleMafiaVote(playerId, targetPlayerId);
                break;
            case "doctor":
                success = handleDoctorVote(playerId, targetPlayerId);
                break;
        }

        log.info(currentGame.getPlayers().get(playerId).getName() + " voted for " + currentGame.getPlayers().get(targetPlayerId).getName());

        return VotePlayerResponse.create(success);
    }

    private boolean handleDoctorVote(String playerId, String targetPlayerId) {
        if(!currentGame.getCurrentPhase().equals(Phase.NIGHT)) {
            log.info("Doctor can only vote during NIGHT phase");
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

        if(currentGame.getPlayers().get(playerId).getRole() != Role.DOCTOR) {
            log.info("Only doctor can protect at night");
            return false;
        }

        currentGame.getPlayers().get(playerId).setNightTargetPlayerId(targetPlayerId);
        return true;
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

    @SuppressWarnings("unchecked")
    public HttpResponse<GamePoliceInspectResponse> policeInspectPlayer(GamePoliceInspectRequest request) {
        var lobbyId = request.getLobbyId();
        var playerId = request.getPlayerId();
        var targetPlayerId = request.getTargetPlayerId();

        if(currentGame == null){
            return (HttpResponse<GamePoliceInspectResponse>) HttpResponse.createFailureResponse("No game is active right now");
        }

        if(!currentGame.getPlayers().containsKey(playerId)){
            return (HttpResponse<GamePoliceInspectResponse>) HttpResponse.createFailureResponse("Given player ID is not in the active game");
        }

        if(!currentGame.getPlayers().containsKey(targetPlayerId)){
            return (HttpResponse<GamePoliceInspectResponse>) HttpResponse.createFailureResponse("Given target player ID is not in the active game");
        }

        if(!Objects.equals(lobbyId, currentGame.getLobbyId())){
            return (HttpResponse<GamePoliceInspectResponse>) HttpResponse.createFailureResponse("Given lobby ID is not active");
        }

        if(currentGame.getPlayers().get(playerId).getRole() != Role.POLICE){
            return (HttpResponse<GamePoliceInspectResponse>) HttpResponse.createFailureResponse("Only police can use this API");
        }

        if(!currentGame.getPlayers().get(playerId).isAlive() || !currentGame.getPlayers().get(targetPlayerId).isAlive()){
            return (HttpResponse<GamePoliceInspectResponse>) HttpResponse.createFailureResponse("source and target must be alive to use this API");
        }

        var playerData  = currentGame.getPlayers().get(playerId);
        if(playerData.isInspectedTonight()) {
            return (HttpResponse<GamePoliceInspectResponse>) HttpResponse.createFailureResponse("Player has already used the inspection ability tonight");
        }

        var hasInspectedTargetAlready = false;
        for(var inspections: playerData.getInspections()) {
            if(inspections.getPlayerId().equals(targetPlayerId)) {
                hasInspectedTargetAlready = true;
                break;
            }
        }

        if(hasInspectedTargetAlready) {
            return (HttpResponse<GamePoliceInspectResponse>) HttpResponse.createFailureResponse("Target player has already been inspected before");
        }

        var targetPlayer = currentGame.getPlayers().get(targetPlayerId);
        var inspection = getInspectionResult(targetPlayer, targetPlayerId);

        playerData.getInspections().add(inspection);
        playerData.setInspectedTonight(true);

        return GamePoliceInspectResponse.create(true);
    }

    private @NonNull InspectionResult getInspectionResult(Player targetPlayer, String targetPlayerId) {
        var inspection = new InspectionResult();
        var playerRole = targetPlayer.getRole();
        inspection.setRoleOrientation(playerRole == Role.MAFIA ? RoleOrientation.BAD : RoleOrientation.GOOD);
        inspection.setPlayerId(targetPlayerId);
        return inspection;
    }

    @SuppressWarnings("unchecked")
    public HttpResponse<Void> chat(ChatRequest request) {
        var lobbyId = request.getLobbyId();
        var playerId = request.getPlayerId();
        var message = request.getMessage();

        if(message == null || message.isEmpty()){
            return (HttpResponse<Void>) HttpResponse.createFailureResponse("Empty message");
        }

        if(message.length() > gameConfigService.getMaxUserMessageLength()) {
            message = message.substring(0, gameConfigService.getMaxUserMessageLength()) + "...";
        }

        if(currentGame == null){
            return (HttpResponse<Void>) HttpResponse.createFailureResponse("No game is active right now");
        }

        if(!currentGame.getPlayers().containsKey(playerId)){
            return (HttpResponse<Void>) HttpResponse.createFailureResponse("Given player ID is not in the active game");
        }

        if(!Objects.equals(lobbyId, currentGame.getLobbyId())){
            return (HttpResponse<Void>) HttpResponse.createFailureResponse("Given lobby ID is not active");
        }
        var player = currentGame.getPlayers().get(playerId);
        currentGame.getAllPlayerMessages().add(new Message(1, Instant.now().toEpochMilli(), player.getName() + ": " + message));
        return new HttpResponse<>();
    }

    @SuppressWarnings("unchecked")
    public HttpResponse<Void> voteSkip(VoteSkipRequest request) {
        var lobbyId = request.getLobbyId();
        var playerId = request.getPlayerId();
        if(currentGame == null){
            return (HttpResponse<Void>) HttpResponse.createFailureResponse("No game is active right now");
        }

        if(!currentGame.getPlayers().containsKey(playerId)){
            return (HttpResponse<Void>) HttpResponse.createFailureResponse("Given player ID is not in the active game");
        }

        if(!Objects.equals(lobbyId, currentGame.getLobbyId())){
            return (HttpResponse<Void>) HttpResponse.createFailureResponse("Given lobby ID is not active");
        }

        var player = currentGame.getPlayers().get(playerId);
        if(!player.isAlive()) {
            return (HttpResponse<Void>) HttpResponse.createFailureResponse("Player is not alive");
        }

        if(currentGame.getCurrentPhase() != Phase.DAY_DISCUSSION) {
            return (HttpResponse<Void>) HttpResponse.createFailureResponse("Skip can be used only during day discussion phase");
        }

        player.setHasVotedToSkipPhase(true);
        return new HttpResponse<>();
    }

    public void skip(int size) {
        var count = 0;
        for(Player player : currentGame.getPlayers().values()){
            player.setHasVotedToSkipPhase(true);
            if(count >= size) {
                break;
            }
        }
    }
}
