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

import java.util.*;

/**
 * Meta-data class to keep various things related to a controller.
 *
 * @author Igor Polevoy
 */
class ControllerMetaData {

    private List<ControllerFilter> controllerFilters = new LinkedList<ControllerFilter>();
    private HashMap<String, List<ControllerFilter>> actionFilterMap = new HashMap<String, List<ControllerFilter>>();


    void addFilters(ControllerFilter[] filters) {
        Collections.addAll(controllerFilters, filters);
    }

    void addFilter(ControllerFilter filter){
        controllerFilters.add(filter);
    }

    
    void addFilters(ControllerFilter[] filters, String[] actionNames) {
        //here we need to remove filters added to this controller if we are adding these filters to actions
        // of this controller.
        controllerFilters.removeAll(Arrays.asList(filters));

        for (String action : actionNames) {
            actionFilterMap.put(action, Arrays.asList(filters));
        }
    }

    /**
     * Returns a collection of filters for this controller. The returned collection will not contain
     * filters for specific actions, only those declared for a controller.
     *
     * @return list of filters in the order in which they were added.
     */
    List<ControllerFilter> getFilters() {
        return Collections.unmodifiableList(controllerFilters);
    }


    protected List<ControllerFilter> getFilters(String action) {
        List<ControllerFilter> filters = actionFilterMap.get(action);
        if (filters != null) {
            return Collections.unmodifiableList(filters);
        }
        return new ArrayList<ControllerFilter>();
    }
}

