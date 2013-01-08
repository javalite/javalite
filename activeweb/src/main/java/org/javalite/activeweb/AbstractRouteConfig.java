package org.javalite.activeweb;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Polevoy: 12/30/11 7:22 PM
 */
public abstract class AbstractRouteConfig extends AppConfig{

    private List<RouteBuilder> routes = new ArrayList<RouteBuilder>();


    public RouteBuilder route(String route){
        RouteBuilder matchedRoute = new RouteBuilder(route);
        routes.add(matchedRoute);
        return matchedRoute;
    }

    protected List<RouteBuilder> getRoutes() {
        return routes;
    }

    protected void clear(){
        routes = new ArrayList<RouteBuilder>();
    }
}