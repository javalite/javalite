package org.javalite.activeweb.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;

public abstract class AppEndpoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Session session;
    private HttpSession httpSession;


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
    public void setHttSession(HttpSession httpSession){
        this.httpSession = httpSession;
    }

    protected final void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public Session getSession() {
        return session;
    }

    public HttpSession getHttpSession() {
        return httpSession;
    }

    public abstract void onMessage(String message);

    public void onClose(CloseReason closeReason) {}

    public final void onError(Throwable throwable) {}

}
