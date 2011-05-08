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

import activeweb.freemarker.FreeMarkerTag;
import activeweb.freemarker.FreeMarkerTemplateManager;
import com.google.inject.Injector;

import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This is an abstract class designed to be overridden in the application. The name for a subclass is:
 * <code>app.config.AppBootstrap</code>. This class is called by the framework during initialization and also executes two
 * other application level classes: <code>app.config.AppControllerConfig</code> and <code>app.config.DbConfig</code>.
 *
 * @see activeweb.AbstractDBConfig
 * @see activeweb.AbstractControllerConfig 
 *
 * @author Igor Polevoy
 */
public abstract class Bootstrap {

    public static void initApp(){

            String initClass = "";
            try {
                //here in this static method we are looking for a subclass
                // of this class by name to invoke a method init() on it.
                initClass = activeweb.Configuration.getBootstrapClassName();
                Class c = Class.forName(initClass);
                Method m = c.getMethod("init");
                Object o = c.newInstance();
                m.invoke(o);
            }
            catch(InvocationTargetException e){
                   throw new InitException("failed to create a new instance of class: " + initClass, e.getTargetException()); 
            }
            catch (Exception e) {
                throw new InitException("failed to create a new instance of class: " + initClass
                        + ", are you sure class exists and it has a default constructor?", e);
            }
    }

    public static void initTemplateManager(ServletContext ctx){
        TemplateManager tm = activeweb.Configuration.getTemplateManager();
        tm.setServletContext(ctx);
    }

    public static void initTemplateManager(String templateLocation){
        TemplateManager tm = activeweb.Configuration.getTemplateManager();
        tm.setTemplateLocation(templateLocation);
    }
    //TODO: write test for custom tag registration
    protected void registerTag(String name, FreeMarkerTag tag){
        ((FreeMarkerTemplateManager)Configuration.getTemplateManager()).registerTag(name, tag);
    }

    public abstract void init();

    public void setInjector(Injector injector){
        ContextAccess.getControllerRegistry().setInjector(injector);
    }
}
