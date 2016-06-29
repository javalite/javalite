/*
Copyright 2009-2016 Igor Polevoy

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

import com.google.inject.Injector;
import org.javalite.activeweb.controller_filters.ControllerFilter;

import java.util.*;

/**
 * Meta-data class to keep various things related to a controller.
 *
 * @author Igor Polevoy
 */
class ControllerMetaData {

    private List<ControllerFilter> controllerFilters = new LinkedList<>();
    private HashMap<String, List<ControllerFilter>> actionFilterMap = new HashMap<>();
    private HashMap<String, List<ControllerFilter>> excludedActionFilterMap = new HashMap<>();


    void addFilters(ControllerFilter[] filters) {
        Collections.addAll(controllerFilters, filters);
    }

    void addFilter(ControllerFilter filter){
        controllerFilters.add(filter);
    }


    void addFiltersWithExcludedActions(ControllerFilter[] filters, String[] excludedActions) {

        for (String action : excludedActions) {
            excludedActionFilterMap.put(action, Arrays.asList(filters));
        }
    }

    void addFilters(ControllerFilter[] filters, String[] actionNames) {
        //here we need to remove filters added to this controller if we are adding these filters to actions
        // of this controller.
        controllerFilters.removeAll(Arrays.asList(filters));

        for (String action : actionNames) {
            actionFilterMap.put(action, Arrays.asList(filters));
        }
    }

//    /**
//     * Returns a collection of filters for this controller. The returned collection will not contain
//     * filters for specific actions, only those declared for a controller.
//     *
//     * @return list of filters in the order in which they were added.
//     */
//    List<ControllerFilter> getFilters() {
//        return Collections.unmodifiableList(controllerFilters);
//    }


    @SuppressWarnings("unchecked")
    protected List<ControllerFilter> getFilters(String action) {
        LinkedList result = new LinkedList();
        result.addAll(controllerFilters);

        List<ControllerFilter> actionFilters = actionFilterMap.get(action);
        if (actionFilters != null) {
            result.addAll(actionFilters);
        }

        List<ControllerFilter> excludedFilters = excludedActionFilterMap.get(action);
        if (excludedFilters != null) {
            for (ControllerFilter excludedFilter : excludedFilters) {
                result.remove(excludedFilter);
            }
        }
        return result;
    }


    protected List<ControllerFilter> getFilters(){
        List<ControllerFilter> allFilters = new LinkedList<>();
        allFilters.addAll(controllerFilters);
        for(List<ControllerFilter> filters: actionFilterMap.values()){
            allFilters.addAll(filters);
        }

        for(List<ControllerFilter> filters: excludedActionFilterMap.values()){
            allFilters.addAll(filters);
        }
        return allFilters;
    }

    protected void injectFilters(Injector injector) {
        for (ControllerFilter controllerFilter : getFilters()) {
            injector.injectMembers(controllerFilter);
        }
    }
}

