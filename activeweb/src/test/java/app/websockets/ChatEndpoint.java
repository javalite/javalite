package app.websockets;

import org.javalite.activeweb.websockets.AppEndpoint;

public class ChatEndpoint extends AppEndpoint {
    @Override
    public void onMessage(String message) {
        sendMessage(" you sent: " + message);
    }
}
