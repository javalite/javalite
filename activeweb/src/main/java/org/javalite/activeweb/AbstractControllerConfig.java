/*
Copyright 2009-2010 Igor Polevoy 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package org.javalite.activeweb;

import org.javalite.activeweb.controller_filters.ControllerFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is to be sub-classed by the application level class called <code>app.config.AppControllerConfig</code>.
 * This class provides ways to bind filters to controllers. It has coarse grain methods for binding as well as
 * fine grained.
 *
 * <p/><p/>
 * See {@link org.javalite.activeweb.controller_filters.ControllerFilter}.
 *
 * <p/><p/>
 * <strong>Filters before() methods are executed in the same order as filters are registered.</strong>
 *
 * <ul>
 *      <li> Adding global filters:{@link #addGlobalFilters(org.javalite.activeweb.controller_filters.ControllerFilter...)}
 *      <li> Adding controller filters:{@link #add(org.javalite.activeweb.controller_filters.ControllerFilter...)} )}
 * </ul>
 * Adding a global filter adds it to all controllers. It makes sense to use this to add timing filters, logging filters,
 * etc.
 *
 * <p/><p/>
 * <strong>Filters' after() methods are executed in the opposite order as filters are registered.</strong>
 *
 *
 * <p/><p/>
 * Here is an example of adding a filter to specific actions:
 *
 * <pre>
 * add(mew TimingFilter(), new DBConnectionFilter()).to(PostsController.class).forActions("index", "show");
 * </pre>
 *
 *
 * @author Igor Polevoy
 */
public abstract class AbstractControllerConfig extends AppConfig {

    //ignore some URLs
    private List<IgnoreSpec> ignoreSpecs = new ArrayList<IgnoreSpec>();

    //exclude some controllers from filters
    private List<ExcludeBuilder> excludeBuilders = new ArrayList<ExcludeBuilder>();

    public class FilterBuilder {
        private ControllerFilter[] filters;
        private Class<? extends AppController>[] controllerClasses;

        protected FilterBuilder(ControllerFilter[] filters) {
            this.filters = filters;
        }

        /**
         * Provides a list of controllers to which filters are added.
         *
         * @param controllerClasses list of controller classes to which filters are added.
         * @return self, usually to run a method {@link #forActions(String...)}.
         */
        public FilterBuilder to(Class<? extends AppController>... controllerClasses) {
            this.controllerClasses = controllerClasses;
            for (Class<? extends AppController> controllerClass : controllerClasses) {
                Context.getControllerRegistry().getMetaData(controllerClass).addFilters(filters);
            }
            return this;
        }

        /**
         * Adds a list of actions for which filters are configured.
         * <p/>
         * Example:
         * <pre>
         * add(mew TimingFilter(), new DBConnectionFilter()).to(PostsController.class).forActions("index", "show");
         * </pre>
         *
         *
         * @param actionNames list of action names for which filters are configured.
         */
        public void forActions(String... actionNames) {
            if (controllerClasses == null)
                throw new IllegalArgumentException("controller classes not provided. Please call 'to(controllers)' before 'forActions(actions)'");

            for (Class<? extends AppController> controllerClass : controllerClasses) {
                Context.getControllerRegistry().getMetaData(controllerClass).addFilters(filters, actionNames);
            }
        }

        /**
         * Excludes actions from filter configuration. Opposite of {@link #forActions(String...)}.
         *
         * @param excludedActions list of actions for which this filter will not apply.
         */
        public void excludeActions(String... excludedActions) {
            if (controllerClasses == null)
                throw new IllegalArgumentException("controller classes not provided. Please call 'to(controllers)' before 'exceptAction(actions)'");

            for (Class<? extends AppController> controllerClass : controllerClasses) {
                Context.getControllerRegistry().getMetaData(controllerClass).addFiltersWithExcludedActions(filters, excludedActions);
            }

        }
    }


    /**
     * Adds a set of filters to a set of controllers.
     * The filters are invoked in the order specified.
     *
     * @param filters filters to be added.
     * @return object with <code>to()</code> method which accepts a controller class. The return type is not important and not used by itself.
     */
    protected FilterBuilder add(ControllerFilter... filters) {
        return new FilterBuilder(filters);
    }

    /**
     * Adds filters to all controllers globally.
     * Example of usage:
     * <pre>
     * ...
     *   addGlobalFilters(new TimingFilter(), new DBConnectionFilter());
     * ...
     * </pre>
     *
     * @param filters filters to be added.
     */
    protected ExcludeBuilder addGlobalFilters(ControllerFilter... filters) {
        ExcludeBuilder excludeBuilder = new ExcludeBuilder(filters);
        excludeBuilders.add(excludeBuilder);
        return excludeBuilder;
    }

    @Override
    public void completeInit() {

        for (ExcludeBuilder excludeBuilder : excludeBuilders) {
            Context.getControllerRegistry().addGlobalFilters(excludeBuilder.getFilters(), excludeBuilder.getExcludeControllerClasses());
        }

        for (IgnoreSpec ignoreSpec: ignoreSpecs) {
            Configuration.addIgnoreSpec(ignoreSpec);
        }
    }


    public class ExcludeBuilder{

        private List<Class<? extends AppController>> excludeControllerClasses = new ArrayList<Class<? extends AppController>>();
        private List<ControllerFilter> filters = new ArrayList<ControllerFilter>();

        public ExcludeBuilder(ControllerFilter[] filters) {
            this.filters.addAll(Arrays.asList(filters));
        }

        /**
         * Pass controllers to this method if you want to exclude supplied filters to be applied to them.
         *
         * @param excludeControllerClasses list of controllers to which these filters do not apply.
         */
        public void exceptFor(Class<? extends AppController>... excludeControllerClasses) {
            this.excludeControllerClasses.addAll(Arrays.asList(excludeControllerClasses));
        }

        public List<Class<? extends AppController>> getExcludeControllerClasses() {
            return excludeControllerClasses;
        }

        public List<ControllerFilter> getFilters() {
            return filters;
        }

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
}
