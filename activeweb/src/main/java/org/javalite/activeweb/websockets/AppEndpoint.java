package org.javalite.activeweb.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;

public abstract class AppEndpoint extends Endpoint implements MessageHandler.Whole<String> {

    private Logger logger;
    private EndpointConfig config;
    private Session session;

    public AppEndpoint() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public final void onOpen(Session session, EndpointConfig config) {

        this.config = config;
        this.session = session;

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                AppEndpoint.this.onMessage(message);
            }
        });
    }

    public final EndpointConfig getConfig() {
        return config;
    }

    protected final Session getSession() {
        return session;
    }

    @Override
    public final void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
    }

    @Override
    public final void onError(Session session, Throwable thr) {
        logger.error("Error while processing a request. Session ID: " + session.getId(), thr);
        super.onError(session, thr);
    }

    public abstract void onMessage(String message);

    protected final void sendMessage(String message){
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.error("Failed to send reply from websocket. ", e);
        }
    }
}
