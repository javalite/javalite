package org.javalite.activeweb;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Polevoy: 12/30/11 7:22 PM
 */
public abstract class AbstractRouteConfig implements InitConfig {

    private List<RouteBuilder> routes = new ArrayList<>();

    //ignore some URLs
    private List<IgnoreSpec> ignoreSpecs = new ArrayList<>();

    private boolean strict = false;



    public RouteBuilder route(String route){
        RouteBuilder matchedRoute = new RouteBuilder(route);
        routes.add(matchedRoute);
        return matchedRoute;
    }

    protected List<RouteBuilder> getRoutes() {
        return routes;
    }

    protected void clear(){
        routes = new ArrayList<>();
    }


    /**
     * Use to ignore requests. Usually you want to ignore requests for static content, such as css files, images. etc.
     * The order of this method compared to route() methods is irrelevant,
     *
     * @param ignores list of regular expressions matching the URI. If an expression matches the request URI, such request ill be ignored
     *                by the framework. It will be processed by container.
     * @return instance of7 IgnoreSpec
     */
    protected IgnoreSpec ignore(String ... ignores){
        IgnoreSpec spec = new IgnoreSpec(ignores);
        ignoreSpecs.add(spec);
        return spec;
    }

    protected final List<IgnoreSpec> getIgnoreSpecs() {
        return ignoreSpecs;
    }

    /**
     * Use in configuration  of custom routes in  order to exclude any other routing paths, such as Standard and Restful.
     * Basically, if this method is called from <code>RouteConfig#init()<code/> method,  the routing system will ONLY allow routes
     * configured in the <code>RouteConfig#init()<code/> method and not any other routes.
     *
     * <br/>
     * If this method is not called, all routes are enabled by default, standard, Restful as well as custom, which might not be a desired
     * configuration, especially if you are building an API with more complexity than simple Restful routes.
     */
    protected void strictMode(){
        strict = true;
    }

    boolean isStrictMode(){
        return strict;
    }

}