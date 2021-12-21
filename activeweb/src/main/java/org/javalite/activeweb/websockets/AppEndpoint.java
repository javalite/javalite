package org.javalite.activeweb.websockets;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;

public abstract class AppEndpoint {

    protected Session session;

    public void setSession(Session session) {
        this.session = session;
    }

    protected final void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public abstract void onMessage(String message);

    public void onClose(Session session, CloseReason closeReason) {}

    public final void onError(Session session, Throwable thr) {}
}
