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

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This is an abstract class designed to be overridden in the application. The name for a subclass is:
 * <code>app.config.AppBootstrap</code>. This class is called by the framework during initialization.
 *
 * @author Igor Polevoy
 * @see AbstractDBConfig
 * @see AbstractControllerConfig
 */
public abstract class Bootstrap implements InitConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);


    /**
     * Called when application bootstraps.
     *
     * @param context app context instance
     */
    @Override
    public abstract void init(AppContext context);

    /**
     * Called when application is destroyed (un-deployed).
     * Override to catch event.
     *
     * @param context app context instance
     */
    public void destroy(AppContext context) {
    }


    /**
     * Subclasses need to override this method to return instance of Injector to use for dependency injection.
     *
     * <strong>
     * <p>
     * This method is NOT USED during testing. Each test class will set its own injector
     * with mocks for testing.
     * </p>
     * <p>
     * It is important to not create a new instance of injector each time this method is called, but
     * rather than return the same instance.
     * </p>
     * <p>
     * No synchronization is needed because the first time
     * this method is used, it is done in one thread.
     * </p>
     *
     * </strong>
     *
     * @return instance of Injector to use to inject dependencies into controllers, filters and tags.
     */
    public Injector getInjector() {
        return null;
    }

    /**
     * Close all services from Guice that implement {@link Destroyable} interface.
     */
    synchronized void destroy() {
        LOGGER.warn("Shutting off all services");
        Injector injector = getInjector();
        if (injector != null) {
            Map<Key<?>, Binding<?>> bindings = injector.getAllBindings();
            bindings.forEach((key, binding) -> {
                Provider p = binding.getProvider();
                if (p != null) {
                    Object service = p.get();
                    if (service != null) {
                        if (Destroyable.class.isAssignableFrom(service.getClass())) {
                            try {
                                LOGGER.warn("Shutting off: " + service);
                                ((Destroyable) service).destroy();
                            } catch (Exception ex) {
                                LOGGER.error("Failed to gracefully destroy " + service + ". Moving on to destroy the rest.", ex);
                            }
                        }
                    }
                }
            });
        }
    }
}
