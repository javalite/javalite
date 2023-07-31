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


import org.javalite.activeweb.controllers.AbstractSystemErrorController;


/**
 * Instance of this class represents a single custom route configured
 * in the RouteConfig for capturing exceptions.
 *
 * @author Igor Polevoy
 */
public class ErrorRouteBuilder {

    private String actionName;
    private AbstractSystemErrorController controllerInstance;
    private Class<? extends AppController> controllerClass;

    /**
     * @param controllerClass class of controller to process errors
     * @return instance of {@link ErrorRouteBuilder}.
     */
    public  <T extends AbstractSystemErrorController> ErrorRouteBuilder to(Class<T> controllerClass) {
        this.controllerClass = controllerClass;
        return this;
    }

    /**
     * @param action name of action to respond to
     * @return instance of {@link ErrorRouteBuilder}.
     */
    public ErrorRouteBuilder action(String action) {

        try{
            RouteUtil.hasAction(controllerClass, action, HttpMethod.GET);
        }catch(Exception e){
            throw new RouteException("Failed  to detect an action: " + action + " on a controller: " + controllerClass, e);
        }

        this.actionName = action;
        return this;
    }

    protected String getActionName() {
        return actionName == null ? actionName = "index": actionName;
    }

    protected AppController getController() {
        return controllerInstance;
    }

    protected Route getRoute(Throwable t) throws ClassLoadException {
         controllerInstance = reloadController();
         controllerInstance.setThrowable(t);
        return new Route(controllerInstance, actionName, HttpMethod.GET);
    }

    private AbstractSystemErrorController reloadController() throws ClassLoadException {

        try {
            return (AbstractSystemErrorController) ControllerFactory.createControllerInstance(controllerClass.getName());
        } catch (ClassLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }

    public Class<? extends AppController> getControllerClass() {
        return controllerClass;
    }
}
