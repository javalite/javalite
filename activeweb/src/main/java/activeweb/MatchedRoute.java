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



/**
 * @author Igor Polevoy
 */
class MatchedRoute<T extends AppController> {
    private String actionName, id;
    private T controller;


    protected MatchedRoute(T controller,  String actionName, String id) {
        this.controller = controller;
        this.actionName = actionName;
        this.id = id;
    }

    protected MatchedRoute(T controller,  String actionName) {
        this(controller, actionName, null);
    }

    protected String getActionName() {
        return actionName;
    }

    protected String getId() {
        return id;
    }

    protected T getController() {
        return controller;
    }

    protected String getControllerClassName(){
        return controller.getClass().getName();
    }
}
