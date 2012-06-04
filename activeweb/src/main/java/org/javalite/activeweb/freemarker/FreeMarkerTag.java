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
package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.ViewException;
import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.template.utility.DeepUnwrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * Convenience class for implementing application - specific tags. 
 *
 * @author Igor Polevoy
 */
public abstract class FreeMarkerTag implements TemplateDirectiveModel {
    
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String context = null;


    /**
     * Provides a logger to a subclass.
     *
     * @return initialized instance of logger. 
     */
    protected Logger logger(){return logger;}

    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        FreeMarkerTL.setEnvironment(env);
        StringWriter sw = new StringWriter();
        if (body != null) {
            body.render(sw);
        }
        try{
            render(params, sw.toString(), env.getOut());
        }catch (ViewException e){
            throw e;
        }catch(Exception e){
            throw new ViewException(e);
        }
    }

    /**
     * Gets an object from context - by name.
     *
     * @param name name of object
     * @return object or null if not found.
     */
    protected TemplateModel get(Object name) {
        try {
            return FreeMarkerTL.getEnvironment().getVariable(name.toString());
        } catch (Exception e) {
            throw new ViewException(e);
        }
    }

    /**
     * Gets an object from context - by name.
     *
     * @param name name of object
     * @return object or null if not found.
     */
    protected Object getUnwrapped(Object name) {
        try {
            return DeepUnwrap.unwrap(get(name));
        } catch (TemplateException e){
            throw new ViewException(e);
        }
    }
    
    protected <T> T getUnwrapped(Object name, Class<T> clazz) {
        return clazz.cast(getUnwrapped(name));
    }

    /**
     * Implement this method ina  concrete subclass.
     *
     * @param params this is a list of parameters as provided to tag in HTML.
     * @param body body of tag
     * @param writer writer to write output to.
     * @throws Exception if any
     */
    protected abstract void render(Map params, String body, Writer writer) throws Exception;


    /**
     * Will throw {@link IllegalArgumentException} if a parameter on the list is missing
     *
     * @param params as a map passed in by Freemarker
     * @param names  list if valid parameter names for this tag.
     */
    protected void validateParamsPresence(Map params, String... names) {
        Util.validateParamsPresence(params, names);
    }

    /**
     * Returns this applications' context path.
     * @return context path.
     */
    protected String getContextPath(){

        if(context != null) return context;

        if(get("context_path") == null){
            throw new ViewException("context_path missing - red alarm!");
        }
        return  get("context_path").toString();
    }


    /**
     * Provides a current session as Map. This map is filled with values from current HTTP session.
     *
     * @return  a current session as Map
     */
    protected Map session(){
        Map session;
        try{
            SimpleHash sessionHash  = (SimpleHash)get("session");
            session = sessionHash.toMap();
        }catch(Exception e){
            logger().warn("failed to get a session map in context, returning session without data!!!", e);
            session = new HashMap();
        }
        return Collections.unmodifiableMap(session);
    }

    /**
     * Processes text as a FreeMarker template. Usually used to process an inner body of a tag.
     *
     * @param text text of a template.
     * @param params map with parameters for processing. 
     * @param writer writer to write output to.
     */
    protected void process(String text, Map params, Writer writer){

        try{
            Template t = new Template("temp", new StringReader(text), FreeMarkerTL.getEnvironment().getConfiguration());
            t.process(params, writer);
        }catch(Exception e){          
            throw new ViewException(e);
        }
    }

    /**
     * Returns a map of all variables in scope.
     * @return map of all variables in scope.
     */
    protected Map getAllVariables(){
        try{
            Iterator names = FreeMarkerTL.getEnvironment().getKnownVariableNames().iterator();
            Map vars = new HashMap();
            while (names.hasNext()) {
                Object name =names.next();
                vars.put(name, get(name.toString()));
            }
            return vars;
        }catch(Exception e){
            throw new ViewException(e);
        }
    }


    /**
     * Use to override context of the application. Usually this is done because  you need
     * to generate special context related paths due to web server configuration
     *
     * @param context this context will be used instead of one provided by Servlet API
     */
    public void overrideContext(String context){
        this.context = context;
    }
}

