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

import javalite.common.Inflector;
import javalite.common.Util;
import javalite.test.jspec.TestException;

/**
 * Super class for controller tests. This class will not open a connection to a test DB.
 *
 * @see {@link activeweb.DBControllerSpec}. 
 * @author Igor Polevoy
 */
public class ControllerSpec extends SpecHelper {

    /**
     * Use this DSL-ish method to send requests to controllers from specs.
     * <strong>Attention</strong>: this method always returns a new object, please string methods one after another - fluent interfaces
     * approach.
     * 
     * @return instance of <code>RequestBuilder</code> with convenience methods.
     */
    protected RequestBuilder request() {
        return new RequestBuilder(getControllerPath(), session());
    }

    
    protected final String getControllerPath() {

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
        String controllerName = specClassName.substring(0, specClassName.lastIndexOf("ControllerSpec"));
        controllerName = Inflector.underscore(controllerName);
        return "/" + (Util.blank(temp)? "": temp + "/") + controllerName;
    }
}

