package org.javalite.activeweb.websockets;


import java.util.ArrayList;
import java.util.List;

/**
 * Subclasses MUST be called  WebSocketConfig...
 */
public abstract class AbstractWebSocketConfig {

    private List<EndpointMapping> mappings = new ArrayList<>();

    protected  abstract void init();

    public EndpointMapping route(String uri){
        return new EndpointMapping(uri);
    }
    public class EndpointMapping {
        private final String uri;
        private Class<? extends AppEndpoint> endpointClass;

        private  EndpointMapping(String uri) {
            this.uri = uri;
        }

        public <T extends AppEndpoint> void to(Class<T> endpointClass) {

            this.endpointClass = endpointClass;
            AbstractWebSocketConfig.this.mappings.add(this);
        }

        public String getUri() {
            return uri;
        }

        public  Class<? extends AppEndpoint> getEndpointClass() {
            return endpointClass;
        }
    }

    protected final List<EndpointMapping> getMappings() {
        return mappings;
    }


    //
//    protected  class ABC extends  AbstractWebsocketConfig{
//
//        @Override
//        protected void init() {
//
//            route("/chat").to(new AppEndpoint() {
//                @Override
//                protected void onMessage() {
//
//                }
//            });
//        }
//    }
}

