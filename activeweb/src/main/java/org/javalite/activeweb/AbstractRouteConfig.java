package org.javalite.activeweb;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Igor Polevoy: 12/30/11 7:22 PM
 */
public abstract class AbstractRouteConfig extends AppConfig{

    private List<RouteBuilder> routes = new ArrayList<RouteBuilder>();

    //ignore some URLs
    private List<IgnoreSpec> ignoreSpecs = new ArrayList<IgnoreSpec>();



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


    /**
     * Use to ignore requests. Usually you want to ignore requests for static content, such as css files, images. etc.
     *
     * @param ignores list of regular expressions matching the URI. If an expression matches the request URI, such request ill be ignored
     *                by the framework. It will be processed by container.
     * @return instance of IgnoreSpec
     */
    protected IgnoreSpec ignore(String ... ignores){
        IgnoreSpec spec = new IgnoreSpec(ignores);
        ignoreSpecs.add(spec);
        return spec;
    }

    protected final List<IgnoreSpec> getIgnoreSpecs() {
        return ignoreSpecs;
    }
}