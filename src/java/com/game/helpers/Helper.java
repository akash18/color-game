package com.game.helpers;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author akash
 */
public class Helper {

    public JsonObject getJsonObject(String message) {
        JsonReader reader = Json.createReader(new StringReader(message));
        return reader.readObject();
    }
    
}
