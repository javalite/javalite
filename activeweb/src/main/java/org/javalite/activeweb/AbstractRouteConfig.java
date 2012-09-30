package org.javalite.activeweb;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Polevoy: 12/30/11 7:22 PM
 */
public abstract class AbstractRouteConfig extends AppConfig{

    private List<Route> routes = new ArrayList<Route>();


    public Route route(String route){
        Route matchedRoute = new Route(route);
        routes.add(matchedRoute);
        return matchedRoute;
    }

    protected List<Route> getRoutes() {
        return routes;
    }

    protected void clear(){
        routes = new ArrayList<Route>();
    }
}