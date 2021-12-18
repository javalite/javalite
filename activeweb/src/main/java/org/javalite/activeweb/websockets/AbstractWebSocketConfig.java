package org.javalite.activeweb.websockets;


import org.javalite.activeweb.AppController;
import org.javalite.activeweb.RouteBuilder;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWebSocketConfig {

    private List<Mapping> mappings = new ArrayList<>();

    protected  abstract void init();

    public Mapping route(String uri){
        return new Mapping(uri);
    }

    protected class Mapping<T extends  AppEndpoint> {
        private final String uri;
        private Class<? extends AppEndpoint> endpointClass;

        private  Mapping(String uri) {
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

    protected final List<Mapping> getMappings() {
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

