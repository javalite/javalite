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

/**
 * Use this as a super class for integration tests that do not require a DB connection.
 * An integration test allows to describe a scenario of actions that span multiple controllers.
 * 
 * @author Igor Polevoy
 */
public class IntegrationSpec extends SpecHelper {

    protected RequestBuilder controller(String controllerName){
        return new RequestBuilder(controllerName, session());
    }

    protected void setTemplateLocation(String templateLocation){
        Configuration.getTemplateManager().setTemplateLocation(templateLocation);
    }

    protected void addFilter(Class<? extends AppController> controllerType, ControllerFilter filter){
        ContextAccess.getControllerRegistry().getMetaData(controllerType).addFilter(filter);
    }
}
