/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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

import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.common.Collections;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is to be sub-classed by the application level class called <code>app.config.AppControllerConfig</code>.
 * This class provides ways to bind filters to controllers.
 * <p>
 * See {@link org.javalite.activeweb.controller_filters.HttpSupportFilter}.
 * <p></p>
 * <strong>Filters before() methods are executed in the same order as filters are registered.</strong>
 * <p></p>
 * <code> Adding controller filters:{@link #add(org.javalite.activeweb.controller_filters.HttpSupportFilter...)} )}</code>
 * <p></p>
 * Not specifying controllers in the <code>to(...)</code> method adds filters to all controllers.
 * <p></p>
 * <strong>Filters' after() methods are executed in the opposite order as filters are registered.</strong>
 * <p></p>
 * Here is an example of adding a filter to specific actions:
 * <pre>
 * add(mew TimingFilter(), new DBConnectionFilter()).to(PostsController.class).forActions("index", "show");
 * </pre>
 *
 * @author Igor Polevoy
 */
public abstract class AbstractControllerConfig implements InitConfig {

    private final List<HttpSupportFilter> allFilters = new ArrayList<>();

    public static class FilterBuilder<T extends AppController> {
        private final List<HttpSupportFilter> filters = new ArrayList<>();


        protected FilterBuilder(HttpSupportFilter[] filters) {
            this.filters.addAll(Collections.list(filters));
        }

        /**
         * Provides a list of controllers to which filters are added.
         *
         * @param controllerClasses list of controller classes to which filters are added.
         * @return self, usually to run a method {@link #forActions(String...)}.
         */
        @SafeVarargs
        public final FilterBuilder<T> to(Class<? extends AppController>... controllerClasses) {
            for (HttpSupportFilter filter : filters) {
                for (var controllerClass : controllerClasses) {
                    Configuration.getFilterMetadata(filter).addController(controllerClass);
                }
            }
            return this;
        }

        /**
         * Adds a list of actions for which filters are configured.
         * <p>
         * Example:
         * <pre>
         * add(mew TimingFilter(), new DBConnectionFilter()).to(PostsController.class).forActions("index", "show");
         * </pre>
         *
         * @param actionNames list of action names for which filters are configured.
         */
        public void forActions(String... actionNames) {
            if (hasControllers()) {
                for (HttpSupportFilter filter : filters) {
                    Configuration.getFilterMetadata(filter).setIncludedActions(actionNames);
                }
            } else {
                throw new IllegalArgumentException("controller classes not provided. Please call 'to(controllers)' before 'forActions(actions)'");
            }
        }

        /**
         * Excludes actions from filter configuration. Opposite of {@link #forActions(String...)}.
         *
         * @param excludedActions list of actions for which this filter will not apply.
         */
        public void excludeActions(String... excludedActions) {
            if (hasControllers()) {
                for (HttpSupportFilter filter : filters) {
                    Configuration.getFilterMetadata(filter).setExcludedActions(excludedActions);
                }
            } else {
                throw new IllegalArgumentException("controller classes not provided. Please call 'to(controllers)' before 'exceptAction(actions)'");
            }
        }

        private boolean hasControllers() {
            boolean hasControllers = false;
            for (HttpSupportFilter filter : filters) {
                if (Configuration.getFilterMetadata(filter).hasControllers()) {
                    hasControllers = true; // at least one is OK
                }
            }
            return hasControllers;
        }

        /**
         * Pass controllers to this method if you want to exclude supplied filters to be applied to them.
         *
         * @param excludeControllerClasses list of controllers to which these filters do not apply.
         */
        @SafeVarargs
        public final void exceptFor(Class<? extends AppController>... excludeControllerClasses) {
            for (HttpSupportFilter filter : filters) {
                Configuration.getFilterMetadata(filter).setExcludedControllers(excludeControllerClasses);
            }
        }
    }


    /**
     * Adds a set of filters to a set of controllers.
     * The filters are invoked in the order specified. Will reject adding the same instance of a filter more than once.
     *
     * @param filters filters to be added.
     * @return object with <code>to()</code> method which accepts a controller class.
     */
    protected FilterBuilder<? extends AppController> add(HttpSupportFilter... filters) {
        for (HttpSupportFilter filter : filters) {
            if(allFilters.contains(filter)){
                throw new IllegalArgumentException("Cannot register the same filter instance more than once.");
            }
        }
        allFilters.addAll(Collections.list(filters));
        return new FilterBuilder<>(filters);
    }


    @Override
    public void completeInit() {
        Configuration.setFilters(allFilters);
    }


}
