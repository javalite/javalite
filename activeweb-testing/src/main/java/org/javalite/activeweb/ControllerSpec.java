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

import org.javalite.common.Util;
import org.junit.jupiter.api.BeforeEach;

/**
 * Super class for controller tests. This class is used by unit tests that test a single controller. Controllers are
 * tested by simulating a web request to a controller (no physical network is involved, and no container initialized).
 * <p/>
 * Subclasses must follow a simple naming convention: subclass name must be
 * made of two words: controller short class name and word "Spec". Example, of there is a controller:
 * <pre>
 * public class GreeterController extends AppController{
 *   ...
 * }
 * </pre>
 * then the test will look like this:
 * <pre>
 *
 * public class GreeterControllerSpec extends ControllerSpec{
 * ...
 * }
 * </pre>
 *
 * ActiveWeb controller specs allow for true TDD, since they do not have a compiler dependency on controllers.
 * You can describe full behavior of your controller before a controller class even exists. Simplest example:
 * <pre>
 * public GreeterControllerSpec extends ControllerSpec{
 *  &#064;Test
 *  public void shouldRespondWithGreetingMessage(){
 *      request().get("index");
 *      a(responseCode()).shouldBeEqual(200);
 *      a(assigns().get("message")).shouldBeEqual("Hello, earthlings!");
 *  }
 * }
 * </pre>
 *
 * In a code snippet above, a request with HTTP GET method is simulated to the GreeterController, index() action.
 * Controller is expected to assign an object called "message" with value "Hello, earthlings!" to a view. 
 *
 * This class will not open a connection to a test DB. If you need a connection,
 * use {@link org.javalite.activeweb.DBControllerSpec}.
 *
 * @see {@link org.javalite.activeweb.DBControllerSpec}.
 * @author Igor Polevoy
 */
public class ControllerSpec extends RequestSpecHelper {

    private String controllerPath;

    public ControllerSpec() {
        Configuration.resetFilters();
        Configuration.setInjector(null);
    }

    @Override @BeforeEach
    public void atStart() {
        super.atStart();
        controllerPath  = getControllerPath();
    }

    /**
     * Use this DSL-ish method to send requests to controllers from specs.
     * <strong>Attention</strong>: this method always returns a new object, please string methods one after another - fluent interfaces
     * approach.
     * 
     * @return instance of <code>RequestBuilder</code> with convenience methods.
     */
    protected RequestBuilder request() {
        return new RequestBuilder(controllerPath, session());
    }

    /**
     * Version of {@link #request()} that also sets integrateViews == true
     *
     * @param integrateViews true to also generate response content
     * @return instance of <code>RequestBuilder</code> with convenience methods.
     * @deprecated does nothing, stop using. Will be deleted  soon. Use {@link #request()} instead.
     */
    protected RequestBuilder request(boolean integrateViews) {
        return new RequestBuilder(controllerPath, session());
    }

    /**
     * Returns a controller path - this includes packages if there are any after "app.controllers".
     *
     * @return   controller path
     */
    protected final String getControllerPath(){
        String controllerClassName = getControllerClassName();
        Class<? extends AppController> controllerClass;
        try{
            controllerClass = (Class<? extends AppController>) Class.forName(controllerClassName);
        }catch(Exception e){
            throw new SpecException("Failed to find a class for: " + controllerClassName, e);
        }
        return Router.getControllerPath(controllerClass);
    }
    
    protected final String getControllerClassName() {

        String packageName = getClass().getPackage().getName();
        if(!packageName.startsWith("app.controllers")){
            throw new SpecException("controller specs must be located in package 'app.controllers' or sub-packages");
        }

        if (!getClass().getSimpleName().endsWith("ControllerSpec"))
            throw new SpecException("Descendant of activeweb.ControllerSpec must be named with: controller name + 'Spec', " +
                    "and because controllers have to have a suffix 'Controller'," +
                    " controller spec classes  must have a suffix: 'ControllerSpec' ");

        String temp = getClass().getName();//full name
        temp = temp.substring(16);
        if(temp.contains(".")){
            temp = temp.substring(0, temp.lastIndexOf("."));// this is sub-package
        }else{
            temp = "";
        }

        String specClassName = getClass().getSimpleName();
        String controllerName = specClassName.substring(0, specClassName.lastIndexOf("Spec"));
        
        return "app.controllers." + (Util.blank(temp)? "": temp + ".") + controllerName; 
    }
}

