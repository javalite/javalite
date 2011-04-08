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
package activeweb;

import activeweb.controller_filters.ControllerFilter;
import com.google.inject.Injector;

import javax.servlet.FilterConfig;
import java.util.*;

/**
 * Registration facility for {@link activeweb.ControllerMetaData}.
 *
 * @author Igor Polevoy
 */
class ControllerRegistry {
    private Map<String, ControllerMetaData> metaDataMap = new HashMap<String, ControllerMetaData>();
    private List<ControllerFilter> globalFilters = new ArrayList<ControllerFilter>();
    private Injector injector;

    // these are not full package names, just partial package names between "app.controllers"
    // and simple name of controller class
    private List<String> controllerPackages;

    private final Object token = new Object();

    protected ControllerRegistry(FilterConfig config) {
        controllerPackages = ControllerPackageLocator.locateControllerPackages(config);
    }


    /**
     * Returns controller metadata for a class.
     *
     * @param controllerClass controller class.
     * @return controller metadata for a controller class.
     */
    protected ControllerMetaData getMetaData(Class<? extends AppController> controllerClass) {
        if (metaDataMap.get(controllerClass.getName()) == null) {
            metaDataMap.put(controllerClass.getName(), new ControllerMetaData());
        }
        return metaDataMap.get(controllerClass.getName());
    }

    protected void addGlobalFilters(ControllerFilter... filters) {
        globalFilters.addAll(Arrays.asList(filters));
    }

    protected List<ControllerFilter> getGlobalFilters() {
        return Collections.unmodifiableList(globalFilters);
    }

    protected void setInjector(Injector injector) {
        this.injector = injector;
    }

    protected Injector getInjector() {
        return injector;
    }

    private boolean filtersInjected = false;

    public void injectFilters() {

        if (!filtersInjected) {
            synchronized (token) {
                if (injector != null) {
                    //inject global filters:
                    for (ControllerFilter controllerFilter : globalFilters) {
                        injector.injectMembers(controllerFilter);
                    }

                    //inject specific controller filters:
                    for (String key : metaDataMap.keySet()) {
                        ControllerMetaData controllerMetaData = metaDataMap.get(key);
                        for (ControllerFilter filter : controllerMetaData.getFilters()) {
                            injector.injectMembers(filter);
                        }
                    }
                }
                filtersInjected = true;
            }

        }

    }

    public List<String> getControllerPackages() {
        return controllerPackages;
    }
}
