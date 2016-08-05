package com.game.websocket;

import com.game.enums.Status;
import com.game.handlers.PlayerSessionHandler;
import com.game.helpers.Helper;
import com.game.model.Game;
import com.game.model.Player;
import com.game.handlers.GameHandler;
import com.game.utils.Constants;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Properties;
import javax.websocket.RemoteEndpoint;

/**
 * This is the server class which listens for the message that comes from client and handle accordingly.
 * @author akash
 */
@ApplicationScoped
@ServerEndpoint("/actions")
public class WebSocketServer {

    @Inject
    PlayerSessionHandler playerSessionHandler;
    @Inject
    GameHandler gameHandler;
    static final Properties prop = new Properties();

    // Loads properties file
    static {
        InputStream input = WebSocketServer.class.getClassLoader().getResourceAsStream(Constants.CONFIG_FILE_NAME);
        try {
            prop.load(input);
            input.close();
        } catch (IOException ex) {
            Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @OnOpen
    public void open(Session session) {
        // Nothing to do while opening session
    }

    @OnClose
    public void close(Session session) {
        playerSessionHandler.removeSession(session);
        Map<Session, Player> sessionPlayerMap = playerSessionHandler.getSessionPlayerMap();
        playerSessionHandler.removePlayer(sessionPlayerMap.get(session));
        try {
            session.close();
        } catch (IOException ex) {
            Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @OnError
    public void onError(Throwable error) {
        Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, error);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        Helper helper = new Helper();
        JsonObject jsonMessage = helper.getJsonObject(message);
        String action = jsonMessage.getString(Constants.ACTION);
        switch (action) {
            case Constants.START_GAME:
                createGame(session, jsonMessage);
                sendPlayerInfoToSession(session);
                break;
            case Constants.JOIN_GAME:
                joinGame(session, jsonMessage);
                sendNewJoineeInfoToAllConnectedSession(session);
                break;
            case Constants.CLICK_BLOCK:
                handleClickEvent(session, jsonMessage);
                break;
        }
    }

    /**
     * This method is used to create a new game and add player to a game and update maps.
     * @param session
     * @param jsonMessage 
     */
    private void createGame(Session session, JsonObject jsonMessage) {
        Game game = new Game();
        gameHandler.addGame(game, jsonMessage);
        gameHandler.insertInMap(session, game);
        addPlayerToGame(session, jsonMessage, game);
    }

    /**
     * This methods adds a player to a game.
     * @param session
     * @param jsonMessage
     * @param game 
     */
    private void addPlayerToGame(Session session, JsonObject jsonMessage, Game game) {
        Player player = new Player(jsonMessage.getString(Constants.NAME), jsonMessage.getString(Constants.COLOR));
        game.getSessions().add(session);
        playerSessionHandler.addPlayer(player);
        playerSessionHandler.addSession(session);
        playerSessionHandler.insertInMap(session, player);
    }

    /**
     * This method sends player information to current session to update scoreboard.
     * @param session 
     */
    private void sendPlayerInfoToSession(Session session) {
        Player player = playerSessionHandler.getSessionPlayerMap().get(session);
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add(Constants.ACTION, "scoreBoard")
                .add(Constants.PLAYER_ID, player.getId())
                .add(Constants.PLAYER_NAME, player.getName())
                .add(Constants.COLOR, player.getColor())
                .add(Constants.SCORE, player.getScore())
                .build();
        playerSessionHandler.sendToSession(session, message.toString());
    }

    /**
     * This methods adds a player to a game with least number of players and 
     * if no current game is on, then update the session with message.
     * @param session
     * @param jsonMessage 
     */
    private void joinGame(Session session, JsonObject jsonMessage) {
        Game game = null;
        if (gameHandler.getGames().isEmpty()) {
            JsonProvider provider = JsonProvider.provider();
            JsonObject message = provider.createObjectBuilder()
                    .add(Constants.ACTION, "noGame")
                    .add(Constants.MSG, "No current games are on... Please create a new Game !!")
                    .build();
            playerSessionHandler.sendToSession(session, message.toString());
        } else {
            int minPlayers = Integer.MAX_VALUE;
            synchronized (this) {
                for (Game g : gameHandler.getGames()) {
                    if (g.getSessions().size() < minPlayers) {
                        game = g;
                        minPlayers = g.getSessions().size();
                    }
                }
            }
            int maxPlayerAllowed = Integer.parseInt(prop.getProperty(Constants.PROP_MAX_PLAYERS));

            if (game != null && (game.getSessions().size() >= maxPlayerAllowed)) {
                JsonProvider provider = JsonProvider.provider();
                JsonObject message = provider.createObjectBuilder()
                        .add(Constants.ACTION, "noGame")
                        .add(Constants.MSG, "All current games are full... Please start a new Game !!")
                        .build();
                playerSessionHandler.sendToSession(session, message.toString());
                return;
            }
            gameHandler.insertInMap(session, game);
            addPlayerToGame(session, jsonMessage, game);
            sendGameStateToSesison(game, session);
            
            if (game != null && game.getStatus().equals(Status.NOT_STARTED)) {
                startTheGame(game);
            }
            // Show the game to current session
            showGame(session);
        }
    }

    /**
     * This methods starts the game.
     * @param game 
     */
    private void startTheGame(Game game) {
        game.setStatus(Status.STARTED);
        sendPopupInfoToPresentSession(game.getSessions().get(0));
    }

    /**
     * This method sends the message to a player who was waiting for another player to join a game.
     * @param session 
     */
    private void sendPopupInfoToPresentSession(Session session) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add(Constants.ACTION, "removeModal")
                .build();
        playerSessionHandler.sendToSession(session, message.toString());
    }

    /**
     * This method returns the score array of all the connected players of a game.
     * @param game
     * @return 
     */
    private JsonArrayBuilder getCurrentScoreBoardBuilder(Game game) {
        JsonArrayBuilder scoreArrayBuilder = Json.createArrayBuilder();
        game.getSessions().stream().map((s) -> playerSessionHandler.getSessionPlayerMap().get(s)).forEach((player) -> {
            scoreArrayBuilder.add(Json.createObjectBuilder()
                    .add(Constants.PLAYER_ID, player.getId())
                    .add(Constants.PLAYER_NAME, player.getName())
                    .add(Constants.COLOR, player.getColor())
                    .add(Constants.SCORE, player.getScore()));
        });
        return scoreArrayBuilder;
    }

    /**
     * This method sends the current state of game to a new joinee.
     * @param game
     * @param session 
     */
    private void sendGameStateToSesison(Game game, Session session) {
        JsonArrayBuilder coloredBlockArrayBuilder = Json.createArrayBuilder();
        for (Map.Entry entry : game.getBlockIndexSessionMap().entrySet()) {
            Player player = playerSessionHandler.getSessionPlayerMap().get((Session) entry.getValue());
            if(player!=null){
            coloredBlockArrayBuilder.add(Json.createObjectBuilder()
                    .add("blockId", (int) entry.getKey())
                    .add("color", player.getColor()));
            }
        }
        JsonArrayBuilder scoreArrayBuilder = getCurrentScoreBoardBuilder(game);

        JsonObject jsonMessage = JsonProvider.provider().createObjectBuilder().
                add(Constants.ACTION, "gameState")
                .add("coloredBlockArray", coloredBlockArrayBuilder)
                .add("scoreArray", scoreArrayBuilder)
                .build();

        playerSessionHandler.sendToSession(session, jsonMessage.toString());
    }

    /**
     * This methods is used to show the game to a player.
     * @param session 
     */
    private void showGame(Session session) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add(Constants.ACTION, "showGame")
                .build();
        playerSessionHandler.sendToSession(session, message.toString());
    }

    /**
     * This method sends the player information who currently joined the game to all the connected session (player) of game
     * to update the scoreboard.
     * @param session 
     */
    private void sendNewJoineeInfoToAllConnectedSession(Session session) {
        Game game = gameHandler.getSessionGameMap().get(session);
        Player player = playerSessionHandler.getSessionPlayerMap().get(session);
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add(Constants.ACTION, "scoreBoard")
                .add(Constants.PLAYER_ID, player.getId())
                .add(Constants.PLAYER_NAME, player.getName())
                .add(Constants.COLOR, player.getColor())
                .add(Constants.SCORE, player.getScore())
                .build();
        String msg = message.toString();
        game.getSessions().stream().forEach((s) -> {
            if(s!=session){
                playerSessionHandler.sendToSession(s, msg);
            }
        });
    }

    /**
     * This method handles the click event.
     * @param session
     * @param jsonMessage 
     */
    private void handleClickEvent(Session session, JsonObject jsonMessage) {
        Player player = playerSessionHandler.getSessionPlayerMap().get(session);
        Game game = gameHandler.getSessionGameMap().get(session);
        player.setScore(player.getScore() + 1);
        if (isGameOver(game)) {
            updateScore(game, player);
            declareWinner(game);
            closeGame(game); //need to take care of it as well
        } else {
            addColorToAllConnectedSessions(session, player, jsonMessage);
            blockUsersInGame(game);
        }
    }

    /**
     * This method adds a color of a player who clicks on block to blocks in all the connected session of game.
     * @param session
     * @param player
     * @param jsonMessage 
     */
    private void addColorToAllConnectedSessions(Session session, Player player, JsonObject jsonMessage) {
        Game game = gameHandler.getSessionGameMap().get(session);
        JsonProvider provider = JsonProvider.provider();
        JsonObject updateMessage = provider.createObjectBuilder()
                .add(Constants.ACTION, jsonMessage.getString(Constants.ACTION))
                .add(Constants.COLOR, jsonMessage.getString(Constants.COLOR))
                .add(Constants.BLOCK_ID, jsonMessage.getString(Constants.BLOCK_ID))
                .add(Constants.PLAYER_ID, player.getId())
                .add(Constants.SCORE, player.getScore())
                .build();

        game.addToBlockSessionMap(Integer.parseInt(jsonMessage.getString(Constants.BLOCK_ID)), session);
        game.setColouredBlocks(game.getColouredBlocks() + 1);

        String message = updateMessage.toString();
        game.getSessions().stream().forEach((s) -> {
            playerSessionHandler.sendToSession(s, message);
        });
    }

    /**
     * This method blocks the players of game when a player clicks on block.
     * @param game 
     */
    private void blockUsersInGame(Game game) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add(Constants.ACTION, "blockUser")
                .add(Constants.DURATION, prop.getProperty(Constants.BLOCKING_SECONDS))
                .add(Constants.MSG, "You are blocked for few seconds !! Please wait..")
                .build();

        String msg = message.toString();
        List<RemoteEndpoint.Async> rm = new ArrayList<>();
        game.getSessions().stream().forEach((session) -> {
            if(session.isOpen()){
                rm.add(session.getAsyncRemote());
            }
        });
        rm.stream().forEach((r) -> {
            r.sendText(msg);
        });
    }

    /**
     * This method updates the score of player in scoreboard in all connected session (players) of game.
     * @param game
     * @param player 
     */
    private void updateScore(Game game, Player player) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject updateMessage = provider.createObjectBuilder()
                .add(Constants.ACTION, "updateScore")
                .add(Constants.PLAYER_ID, player.getId())
                .add(Constants.SCORE, player.getScore())
                .build();

        String message = updateMessage.toString();
        game.getSessions().stream().forEach((session) -> {
            playerSessionHandler.sendToSession(session, message);
        });
    }

    /**
     * This method finds player with max score and update the player with message whether player won/lost.
     * @param game 
     */
    private void declareWinner(Game game) {
        int maxScore = 0;
        for (Session s : game.getSessions()) {
            Player player = playerSessionHandler.getSessionPlayerMap().get(s);
            if (player.getScore() > maxScore) {
                maxScore = player.getScore();
            }
        }

        for (Session s : game.getSessions()) {
            Player player = playerSessionHandler.getSessionPlayerMap().get(s);
            if (player.getScore() == maxScore) {
                player.setWinner(true);
            }
        }

        JsonProvider provider = JsonProvider.provider();
        JsonObjectBuilder jsonObjectBuilder = provider.createObjectBuilder();

        game.getSessions().stream().forEach((s) -> {
            String message = "You Lost !!";
            Player player = playerSessionHandler.getSessionPlayerMap().get(s);
            if (player.isWinner()) {
                message = "You Won !!";
            }
            JsonObject jsonMessage = jsonObjectBuilder.add(Constants.ACTION, "gameOver")
                    .add(Constants.NAME, player.getName())
                    .add(Constants.MSG, message)
                    .build();

            playerSessionHandler.sendToSession(s, jsonMessage.toString());
        });
    }

    /**
     * This method removes the game.
     * @param game 
     */
    private void closeGame(Game game) {
        gameHandler.removeGame(game);
        game = null;
    }

    /**
     * This method checks if the game is over or not.
     * @param game
     * @return 
     */
    private boolean isGameOver(Game game) {
        return (game.getColouredBlocks() + 1) == game.getTotalBlocks();
    }
}