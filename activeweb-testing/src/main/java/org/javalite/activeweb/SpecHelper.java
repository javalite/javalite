/*
Copyright 2009-2014 Igor Polevoy

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


import com.google.inject.Injector;
import org.javalite.activeweb.freemarker.FreeMarkerTag;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import org.javalite.common.Convert;
import org.javalite.test.jspec.JSpecSupport;
import org.javalite.test.jspec.TestException;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Util.blank;

/**
 * This class is not used directly in applications.
 *
 * @author Igor Polevoy
 */
public class SpecHelper extends JSpecSupport{

    private SessionTestFacade sessionFacade;


    @Before
    public void atStart() {
        sessionFacade = new SessionTestFacade(new MockHttpSession());
        setTemplateLocation("src/main/webapp/WEB-INF/views");//default location of all views

        Context.setTLs(null, new MockHttpServletResponse(), new MockFilterConfig(),
                new ControllerRegistry(new MockFilterConfig()), new AppContext(), new RequestContext(), null);
    }

    @After
    public void afterEnd(){
        Context.clear();
    }

    /**
     * @param location this is a relative location starting from the module root, intended for testing.
     */
    protected void setTemplateLocation(String location){
        Configuration.getTemplateManager().setTemplateLocation(location);
    }

    /**
     * Use to set injector for current test.
     *
     * @param injector injector to source dependencies form.
     */
    protected void setInjector(Injector injector){
        Context.getControllerRegistry().setInjector(injector);
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
