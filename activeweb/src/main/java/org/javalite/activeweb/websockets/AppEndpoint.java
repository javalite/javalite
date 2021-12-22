package org.javalite.activeweb.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;

public abstract class AppEndpoint {
    private Logger logger = LoggerFactory.getLogger(getClass());

    protected Session session;


    protected final void logInfo(String info){
        logger.info(info);
    }

    protected final void logDebug(String info){
        logger.debug(info);
    }

    protected final void logWarning(String info){
        logger.warn(info);
    }

    protected final void logWarning(String info, Throwable e){
        logger.warn(info, e);
    }

    protected final void logError(String info){
        logger.error(info);
    }

    protected final void logError(Throwable e){
        logger.error("", e);
    }

    protected final void logError(String info, Throwable e){
        logger.error(info, e);
    }

    final void setSession(Session session) {
        this.session = session;
    }

    protected final void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public abstract void onMessage(String message);

    public void onClose(Session session, CloseReason closeReason) {}

    public final void onError(Session session, Throwable thr) {}
}
