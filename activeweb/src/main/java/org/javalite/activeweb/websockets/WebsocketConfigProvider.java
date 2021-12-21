package org.javalite.activeweb.websockets;

import org.javalite.activeweb.Configuration;
import org.javalite.activeweb.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class WebsocketConfigProvider implements ServerApplicationConfig {

    private String configClassName = "app.config.WebSocketConfig";

    private Logger LOGGER = LoggerFactory.getLogger(WebsocketConfigProvider.class);

    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
        Set<ServerEndpointConfig> result = new HashSet<>();
        try {
            Class<?> wsClass = Class.forName(configClassName);
            AbstractWebSocketConfig config = (AbstractWebSocketConfig) wsClass.getDeclaredConstructor().newInstance();
            config.init();
            List<AbstractWebSocketConfig.EndpointMapping> mappings = config.getMappings();

            for (AbstractWebSocketConfig.EndpointMapping mapping : mappings) {
                LOGGER.info("Configuring a websocket" + mapping.getEndpointClass() + " for a path: " + mapping.getUri());
                ServerEndpointConfig sec = ServerEndpointConfig.Builder.create(EndpointDispatcher.class, mapping.getUri()).build();
                result.add(sec);
                Configuration.addEndpointMapping(mapping);
            }
        } catch (ClassNotFoundException ignore) {
            LOGGER.info("Failed to find an instance of " + configClassName + ", proceeding without WebSockets");
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ConfigurationException("Failed to configure websockets.", e);
        }
        return result;
    }

    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
        return Collections.emptySet();
    }
}
