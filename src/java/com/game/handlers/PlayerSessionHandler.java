package com.game.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.websocket.Session;

import com.game.model.Player;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

/**
 * This class is used to handle the map of player and session. 
 * Also it adds and remove the player and session, also sends message to session (Client)
 * 
 * @author akash
 */
@ApplicationScoped
public class PlayerSessionHandler {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
    private static final Set<Player> players = Collections.synchronizedSet(new HashSet<Player>());
    private static final Map<Session, Player> sessionPlayerMap = Collections.synchronizedMap(new HashMap<Session,Player>());
    private int playerId = 0;

    public void addSession(Session session) {
        sessions.add(session);
    }
        
    public void insertInMap(Session session, Player player){
        sessionPlayerMap.put(session, player);
    }
    
    public Map<Session,Player> getSessionPlayerMap(){
        return sessionPlayerMap;
    }
    
    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public void addPlayer(Player player) {
        player.setId(playerId);
        players.add(player);
        playerId++;
    }

    public void removePlayer(Player player) {
        if (player != null) {
            players.remove(player);
        }
    }

    public void sendToSession(Session session, String message) {
        if(session.isOpen()){
            session.getAsyncRemote().sendText(message);
        }
    }

    private Player getPlayerById(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    private void sendToAllConnectedSessions(String message) {
        sessions.stream().forEach((session) -> {
            sendToSession(session, message);
        });
    }
}
