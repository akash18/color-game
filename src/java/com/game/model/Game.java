package com.game.model;

import com.game.enums.Status;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.websocket.Session;

/**
 *
 * @author akash
 */
public class Game {

    private int id;
    // Corresponds to list of players.
    private List<Session> sessions = new ArrayList<>();
    private Status status =  Status.NOT_STARTED;
    private int totalBlocks;
    private int colouredBlocks;
    private Map<Integer, Session> blockIndexSessionMap = new HashMap<>();
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> session) {
        this.sessions = session;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }   

    public int getColouredBlocks() {
        return colouredBlocks;
    }

    public void setColouredBlocks(int colouredBlocks) {
        this.colouredBlocks = colouredBlocks;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    public Map<Integer, Session> getBlockIndexSessionMap() {
        return blockIndexSessionMap;
    }
    
    public void addToBlockSessionMap(int blockId, Session session) {
        this.blockIndexSessionMap.put(blockId, session);
    }
}
