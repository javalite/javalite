package org.javalite.activeweb.websockets;

import org.javalite.json.JSONMap;
import org.javalite.json.JSONParseException;

import static org.javalite.json.JSONHelper.toJSONMap;

public abstract class JSONMapEndpoint extends AppEndpoint {
    @Override
    public void onMessage(String json) {
        try {
            onJSONMap(toJSONMap(json));
        } catch (JSONParseException e) {
            throw new WebSocketException("The input is not JSON map.", e);
        }
    }

    public abstract void onJSONMap(JSONMap map);
}
