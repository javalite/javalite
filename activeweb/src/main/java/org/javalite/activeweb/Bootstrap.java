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

import org.javalite.activeweb.freemarker.FreeMarkerTag;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an abstract class designed to be overridden in the application. The name for a subclass is:
 * <code>app.config.AppBootstrap</code>. This class is called by the framework during initialization.
 *
 * @see AbstractDBConfig
 * @see AbstractControllerConfig
 *
 * @author Igor Polevoy
 */
public abstract class Bootstrap extends AppConfig{

    private static final Logger  LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    @Deprecated
    protected void registerTag(String name, FreeMarkerTag tag){
        ((FreeMarkerTemplateManager)Configuration.getTemplateManager()).registerTag(name, tag);
    }

    /**
     * Called when application is bootstraps.
     *
     * @param context app context instance
     */
    @Override
    public abstract void init(AppContext context);

    /**
     * Called when application is destroyed (un-deployed).
     * Override to catch event.
     *
     * 
     * @param context app context instance
     */
    public void destroy(AppContext context){}

    /**
     *
     * Sets a Google Guice Injector to use to inject dependencies into filters, controllers and tags.
     *
     * <p>
     *     This method has no effect during testing. Each test class will set its own injector
     *     with mocks for testing.
     * </p>
     *
     * @deprecated This method will be removed in future versions. Instead, override {@link #getInjector()} method
     * to produce your injector.
     *
     * @param injector Injector instance to use for dependency injection.
     */
    public void setInjector(Injector injector){
        if(!Configuration.isTesting()){
            LOGGER.warn("WARNING!!! Method Bootstrap#setInjector(Injector) is deprecated and will be removed in future versions. Please switch to Bootstrap#getInjector() instead.");
            Context.getControllerRegistry().setInjector(injector);
        }
    }

    /**
     * Subclasses need to override this method to return instance of Injector to use for dependency injection.
     *
     * <strong>
     *     <p>
     *     This method is NOT USED during testing. Each test class will set its own injector
     *     with mocks for testing.
     *     </p>
     *     <p>
     *     It is important to not create a new instance of injector each time this method is called, but
     *     rather than return the same instance.
     *     </p>
     *     <p>
     *     No synchronization is needed because the first time
     *     this method is used, it is done in one thread.
     *     </p>
     *
     * </strong>
     *
     * @return instance of Injector to use to inject dependencies into controllers, filters and tags.
     */
    public Injector getInjector(){
        return null;
    };
}
