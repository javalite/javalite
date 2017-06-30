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


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.javalite.activeweb.freemarker.FreeMarkerTag;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import org.javalite.common.Convert;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.list;

/**
 * This class is not used directly in applications.
 *
 * @author Igor Polevoy
 */
public class SpecHelper implements JSpecSupport{

    private SessionTestFacade sessionFacade;


    @Before
    public void atStart() {
        sessionFacade = new SessionTestFacade(new MockHttpSession());
        setTemplateLocation("src/main/webapp/WEB-INF/views");//default location of all views

        RequestContext.setTLs(null, new MockHttpServletResponse(), new MockFilterConfig(),
                new AppContext(), new RequestVo(), null);

    }

    @After
    public void afterEnd(){
        RequestContext.clear();
    }

    /**
     * @param location this is a relative location starting from the module root, intended for testing.
     */
    protected void setTemplateLocation(String location){
        Configuration.getTemplateManager().setTemplateLocation(location);
    }


    /**
     * Convenience method: allows to set services without explicitly configuring a new module for mocking.
     * All services are set as "eagerSingleton".
     *
     * <p>Example:</p>
     *
     * <pre>
     * public void before(){
     *    injector().bind(Greeter.class).to(GreeterMock.class)
     *              .bind(Redirector.class).to(RedirectorImpl.class).create();
     * }
     * </pre>
     *
     * <p>
     *An example where the first class is also an implementation:
     * </p>
     *
     * <pre>
     * public void before(){
     *    injector().bind(Greeter.class).create();
     * }
     * </pre>

     *
     * The instance of a new injector will also be added to the current context and used to inject
     * services into filters and controllers executing by this test.
     * <p>
     *     Each test method can potentially setup its own injector like this, and not interfere with previous settings.
     * </p>
     *
     * If you need more advanced settings, use {@link #createInjector(AbstractModule)} or {@link #setInjector(Injector)} methods.
     *
     * @return instance of dynamically created injector with interfaces and services already set.
     */
    protected DynamicBuilder injector(){
        return new DynamicBuilder();
    }

    protected class DynamicBuilder{
        List<List<Class>> pairs = new ArrayList<>();

        /**
         * @param interfaceClass class of an interface for Guice injector
         */
        public DynamicBuilder bind(Class interfaceClass){
            pairs.add(list(interfaceClass));
            return this;
        }

        /**
         * This method is optional. If omitted, the  class provided for the interface to {@link #bind(Class)}
         * method will be used as implementation as well.
         *
         * @param implementationClass implementation of an interface for Guice injector
         */
        public DynamicBuilder to(Class implementationClass){
            pairs.get(pairs.size() - 1).add(implementationClass);
            return this;
        }

        public Injector create(){
            DynamicModule dynamicModule = new DynamicModule(pairs);
            Injector injector = Guice.createInjector(dynamicModule);
            SpecHelper.this.setInjector(injector);
            return injector;
        }
    }

    private class DynamicModule extends AbstractModule{
        private List<List<Class>> pairs = new ArrayList<>();

        public DynamicModule(List<List<Class>> pairs) {
            this.pairs = pairs;
        }

        @Override
        protected void configure() {
            for (List<Class> pair : pairs) {
                if(pair.size() == 1){
                    bind(pair.get(0)).asEagerSingleton();
                }else {
                    bind(pair.get(0)).to(pair.get(1)).asEagerSingleton();
                }
            }
        }
    }

    /**
     * Use to set injector for current test.
     * <p>
     *     How to override some services for tests:
     *
     *     <pre>
     *         Injector injector  = Guice.createInjector(Modules.override(new CommonModule()).with(new CommonModuleMock());
     *         setInjector(injector);
     *     </pre>
     * </p>
     *
     * @param injector injector to source dependencies form.
     */
    protected void setInjector(Injector injector){
        Configuration.setInjector(injector);
    }

    /**
     * This is a convenience  method for setting Guice modules and service mocks.
     *
     * <p>
     *     For instance, consider this code:
     *
     *     <pre>
     *         Injector injector  = Guice.createInjector(Modules.override(new CommonModule()).with(new CommonModuleMock());
     *         setInjector(injector);
     *     </pre>
     *
     *     The mock classes are specified  inside  the  class <code>CommonModuleMock</code>, which means that you
     *     have to write the module class. This process is tedious and inflexible in a large project.
     *</p>
     * <p>
     *     The <code>createInjector(..)</code> method allows for a more elegant way of overriding real services with mocks:
     *     <pre>
     *         Injector injector = createInjector(new CommonModule())
     *                                          .override(EmailService.class).with(EmailServiceMock.class)
     *                                          .override(SmsService.class).with(SmsServiceMock.class).
     *                                          .create();
     *         setInjector(injector);
     *     </pre>
     *
     *      As you can see, the is no longer need for writing  a mock module.

     * </p>
     *
     * @param module - main module you want to set on a spec. This module may include services you need to override.
     *
     * @return instance of Injector with services in the main module overridden by provided mocks.
     */
    protected <T extends AbstractModule> ModuleBuilder createInjector(T module){
        return new ModuleBuilder(module);
    }

    public class ModuleBuilder{

        private List<Class> interfaceClasses = new ArrayList<>();
        private List<Class> mockClasses = new ArrayList<>();
        private Module module;

        protected ModuleBuilder(Module module) {
            this.module = module;
        }

        /**
         * Specifies what interface to override with a mock.
         *
         * @param interfaceClass class of an service interface  to override by a mock.
         * @return instance of {@link ModuleBuilder}
         */
        public ModuleBuilder override(Class interfaceClass){
            interfaceClasses.add(interfaceClass);
            return this;
        }

        /**
         * Specifies what mock to use to override a real service in a module.
         *
         * @param mockClass a mock service class to override a real service in the module as eager singletone.
         * @return instance of {@link ModuleBuilder}
         */
        public ModuleBuilder with(Class mockClass){
            mockClasses.add(mockClass);
            return this;
        }

        private class MockModule extends AbstractModule {
            private Class  interfaceClass, mockClass;

            private MockModule(Class interfaceClass, Class mockClass) {
                this.interfaceClass = interfaceClass;
                this.mockClass = mockClass;
            }

            @Override @SuppressWarnings("unchecked")
            protected void configure() {
                bind(interfaceClass).to(mockClass).asEagerSingleton();
            }
        };

        /**
         * Terminal method of a builder. Use to generate an instance of  Injector.
         *
         * @return properly configured instance of injector, with all
         */
        public Injector create(){
            List<Module> modules = new ArrayList<>();
            for (int i = 0; i < interfaceClasses.size(); i++) {
                modules.add(new MockModule(interfaceClasses.get(i), mockClasses.get(i) ));
            }
            return Guice.createInjector(Modules.override(module).with(modules));
        }
    }



    /**
     * Registers a single custom tag. You can call this method as many times as necessary to register multiple tags in tests.
     * If you want to use all tags that you registered in <code>app.config.AppBootstrap</code> class, then you an
     * option of using <code>AppIntegrationSpec</code> as a super class.
     *
     * @param name tag name where name is a part of the tag on page like so: <code><@name...</code>.
     * @param tag instance of tag to register.
     */
    protected void registerTag(String name, FreeMarkerTag tag){
        ((FreeMarkerTemplateManager)Configuration.getTemplateManager()).registerTag(name, tag);
    }


    /**
     * Allows access to session in test context.
     *
     * @return object allowing access to session in test context.
     */
    protected SessionTestFacade session(){
        return sessionFacade;
    }


    /**
     * Returns a named flash value assigned to session by controller.
     *
     * @param name name of flash value.
     * @return flash value assigned to session by controller.
     */
    protected Object flash(String name){
        if(session().get("flasher") == null)
            return null;

        Map flasher = (Map) session().get("flasher");
        return flasher.get(name) == null? null :flasher.get(name);
    }

    /**
     * Tests if flash by name exists.
     *
     * @param name name in question
     * @return true if flash exists, false if not. Will return <code>true</code> even if flash by name exists,
     * but its value is <code>null</code>.
     */
    protected Object flashExists(String name){
        Map flasher = (Map) session().get("flasher");
        return flasher != null && flasher.containsKey(name);
    }

    /**
     * Returns a named flash value assigned to session by controller.
     *
     * @param name name of flash value.
     * @param type type to be returned
     * @return flash value assigned to session by controller.
     */
    protected  <T>  T flash(String name, Class<T> type){
        return (T) flash(name);
    }


    /**
     * Convenience method, sets an object on a session. Equivalent of:
     * <pre>
     * <code>
     *     session().put(name, value)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @param value object itself.
     */
    protected void session(String name, Serializable value){
        session().put(name, value);
    }

    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object,
     * @return session object.
     */
    protected Object sessionObject(String name){
        return session().get(name);
    }

    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     String val = (String)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected String sessionString(String name){
        return (String)session().get(name);
    }



    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     Integer val = (Integer)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected Integer sessionInteger(String name){
        return Convert.toInteger(session().get(name));
    }


    /**
     * Returns object from session that is already cast to expected type.
     *
     * @param name name of object in session
     * @param type expected type.
     * @return object from session that is already cast to expected type.
     */
    protected  <T>  T session(String name, Class<T> type){
        return (T) session().get(name);
    }

    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     Boolean val = (Boolean)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected Boolean sessionBoolean(String name){
        return Convert.toBoolean(session().get(name));
    }

    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     Double val = (Double)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected Double sessionDouble(String name){
        return Convert.toDouble(session().get(name));
    }

    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     Float val = (Float)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected Float sessionFloat(String name){
        return Convert.toFloat(session().get(name));
    }

    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     Long val = (Long)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected Long sessionLong(String name){
        return Convert.toLong(session().get(name));
    }

    /**
     * Returns true if session has named object, false if not.
     *
     * @param name name of object.
     * @return true if session has named object, false if not.
     */
    protected boolean sessionHas(String name){
        return session().get(name) != null;

    }

}
