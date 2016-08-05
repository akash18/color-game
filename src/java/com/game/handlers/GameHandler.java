package com.game.handlers;

import com.game.enums.Status;
import com.game.model.Game;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.websocket.Session;

/**
 * This class is used to handle the game corresponding to session. 
 * Also it is used to add, remove game.
 * @author akash
 */
@ApplicationScoped
public class GameHandler {

    private int gameId = 0;
    private static final List<Game> games = Collections.synchronizedList(new ArrayList<Game>());
    private static final Map<Session, Game> sessionGameMap = Collections.synchronizedMap(new HashMap<Session,Game>());

    public void insertInMap(Session session, Game game){
        sessionGameMap.put(session, game);
    }

    public Map<Session, Game> getSessionGameMap() {
        return sessionGameMap;
    }
    
    public void addGame(Game game, JsonObject jsonMessage) {
        game.setId(gameId);
        game.setTotalBlocks(jsonMessage.getInt("totalBlocks"));
        game.setStatus(Status.NOT_STARTED);
        games.add(game);
        gameId++;
    }

    public List<Game> getGames() {
        return games;
    }
    
    public void removeGame(Game game) {
        games.remove(game);
    }

    public Game getGame(int gameId) {
        for (Game game : getGames()) {
            if (game.getId() == gameId) {
                return game;
            }
        }
        return null;
    }

    public int getGameId() {
        return gameId;
    }
}
