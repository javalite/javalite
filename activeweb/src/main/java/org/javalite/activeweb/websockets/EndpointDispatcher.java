package org.javalite.activeweb.websockets;

import org.javalite.activeweb.Configuration;
import org.javalite.activeweb.DynamicClassFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;


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
            session.addMessageHandler(this);

        } catch (Exception e) {
            logger.error("Failed to map an endpoint to path: " + session.getRequestURI().getPath(), e);
        }
    }

    @Override
    public final void onClose(Session session, CloseReason closeReason) {
        endpoint.onClose(session, closeReason);
    }

    @Override
    public final void onError(Session session, Throwable e) {
        endpoint.onError(session, e);
    }

    public void onMessage(String message){
        endpoint.onMessage(message);
    }
}
