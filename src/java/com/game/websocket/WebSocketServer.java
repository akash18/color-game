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
 * 
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

    private void createGame(Session session, JsonObject jsonMessage) {
        Game game = new Game();
        gameHandler.addGame(game, jsonMessage);
        gameHandler.insertInMap(session, game);
        addPlayerToGame(session, jsonMessage, game);
    }

    private void addPlayerToGame(Session session, JsonObject jsonMessage, Game game) {
        Player player = new Player(jsonMessage.getString(Constants.NAME), jsonMessage.getString(Constants.COLOR));
        game.getSessions().add(session);
        playerSessionHandler.addPlayer(player);
        playerSessionHandler.addSession(session);
        playerSessionHandler.insertInMap(session, player);
    }

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

    private void startTheGame(Game game) {
        game.setStatus(Status.STARTED);
        sendPopupInfoToPresentSession(game.getSessions().get(0));
    }

    private void sendPopupInfoToPresentSession(Session session) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add(Constants.ACTION, "removeModal")
                .build();
        playerSessionHandler.sendToSession(session, message.toString());
    }

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

    private void showGame(Session session) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add(Constants.ACTION, "showGame")
                .build();
        playerSessionHandler.sendToSession(session, message.toString());
    }

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

    private void closeGame(Game game) {
        gameHandler.removeGame(game);
        game = null;
    }

    private boolean isGameOver(Game game) {
        return (game.getColouredBlocks() + 1) == game.getTotalBlocks();
    }
}
