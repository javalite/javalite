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
    public class IgnoreSpec {
        private List<Pattern> ignorePatterns = new ArrayList<Pattern>();
        private String exceptEnvironment;

        protected IgnoreSpec(String[] ignores){
            for (String ignore : ignores) {
                ignorePatterns.add(Pattern.compile(ignore));
            }
        }

        protected boolean ignores(String path){
            boolean matches = false;
            for (Pattern pattern : ignorePatterns) {
                Matcher m = pattern.matcher(path);
                matches = m.matches();
                if (exceptEnvironment != null) {
                    String env = Configuration.getEnv();
                    if (matches && exceptEnvironment.equals(Configuration.getEnv())) {
                        matches = false; //-- need to ignore
                    }
                }
            }
            return matches;
        }

        /**
         * Sets an environment in which NOT TO ignore a URI. Typical use is to process some URIs in
         * development environment, such as compile CSS, or do special image processing. In other environments,
         * this URL will be ignored, given that resource is pre-processed and available from container.
         *
         * @param environment name of envoronment in which NOT to ignore this URI.
         */
        public void exceptIn(String environment){
            this.exceptEnvironment = environment;
        }
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

    @Override
    public void completeInit() {
        for (IgnoreSpec ignoreSpec: ignoreSpecs) {
            Configuration.addIgnoreSpec(ignoreSpec);
        }
    }
}