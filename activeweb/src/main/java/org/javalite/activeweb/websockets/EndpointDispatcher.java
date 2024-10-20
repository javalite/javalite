package org.javalite.activeweb.websockets;

import org.javalite.activeweb.Configuration;
import org.javalite.activeweb.DynamicClassFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import javax.websocket.*;


/**
 * Interesting advice: https://stackoverflow.com/questions/50114490/java-websocket-session-times-out-regardless-the-value-of-setmaxidletimeout
 * Another: https://yishanhe.net/how-to-keep-your-websocket-session-alive/
 * Java Client: https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-client/src/main/java/com/baeldung/websocket/client
 */
public class EndpointDispatcher extends Endpoint implements MessageHandler.Whole<String> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private AppEndpoint endpoint;

    public EndpointDispatcher() {}

    @Override
    public final void onOpen(Session session, EndpointConfig config) {

        try {
            Class<? extends AppEndpoint> clazz = Configuration.getAppEndpointClass(session.getRequestURI().getPath());
            endpoint = DynamicClassFactory.createInstance(clazz.getName(), AppEndpoint.class);
            endpoint.setSession(session);
            HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
            endpoint.setHttSession(httpSession);
            session.addMessageHandler(this);

        } catch (Exception e) {
            logger.error("Failed to map an endpoint to path: " + session.getRequestURI().getPath(), e);
        }
    }

    @Override
    public final void onClose(Session session, CloseReason closeReason) {
        endpoint.onClose(closeReason);
    }

    @Override
    public final void onError(Session session, Throwable e) {
        endpoint.onError(e);
    }

    public void onMessage(String message){
        endpoint.onMessage(message);
    }
}
