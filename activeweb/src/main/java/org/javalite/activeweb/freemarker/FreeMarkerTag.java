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
package org.javalite.activeweb.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.template.SimpleHash;
import freemarker.template.utility.DeepUnwrap;
import org.javalite.activeweb.*;
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
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String context;


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


    /**
     * Returns value of one named parameter from request. If this name represents multiple values, this
     * call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of request parameter.
     */
    protected  String param(String name){
        return RequestUtils.param(name);
    }


    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    protected  String getId(){
        return RequestUtils.getId();


    }


    /**
     * Returns a format part of the URI, or null if URI does not have a format part.
     * A format part is defined as part of URI that is trailing after a last dot, as in:
     *
     * <code>/books.xml</code>, here "xml" is a format.
     *
     * @return format part of the URI, or nul if URI does not have it.
     */
    protected  String format(){
        return RequestUtils.format();
    }


    /**
     * Returns instance of {@link org.javalite.activeweb.AppContext}.
     *
     * @return instance of {@link org.javalite.activeweb.AppContext}.
     */
    protected  AppContext appContext(){
        return RequestUtils.appContext();
    }

    /**
     * Returns true if this request is Ajax.
     *
     * @return true if this request is Ajax.
     */
    protected  boolean isXhr(){
        return RequestUtils.isXhr();
    }


    /**
     * Helper method, returns user-agent header of the request.
     *
     * @return user-agent header of the request.
     */
    protected  String userAgent(){
        return RequestUtils.userAgent();
    }

    /**
     * Synonym for {@link #isXhr()}.
     */
    protected  boolean xhr(){
        return RequestUtils.xhr();
    }


    /**
     * Returns instance of {@link org.javalite.activeweb.Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link org.javalite.activeweb.Route}
     */
    protected  Route getRoute(){
        return RequestUtils.getRoute();
    }


    /**
     * Tests if a request parameter exists. Disregards the value completely - this
     * can be empty string, but as long as parameter does exist, this method returns true.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     */
    protected boolean exists(String name){
        return RequestUtils.exists(name);
    }

    /**
     * Synonym of {@link #exists(String)}.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     */
    protected boolean requestHas(String name){
        return RequestUtils.requestHas(name);
    }


    /**
     * Returns local host name on which request was received.
     *
     * @return local host name on which request was received.
     */
    protected  String host() {
        return RequestUtils.host();
    }


    /**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     */
    protected   String ipAddress() {
        return RequestUtils.ipAddress();
    }





    /**
     * This method returns a protocol of a request to web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Proto</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #protocol()} method is used.
     *
     * @return protocol of web server request if <code>X-Forwarded-Proto</code> header is found, otherwise current
     * protocol.
     */
    protected  String getRequestProtocol(){
        return RequestUtils.getRequestProtocol();
    }

    /**
     * This method returns a port of a web server if this Java container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Port</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #port()} method is used.
     *
     * @return port of web server request if <code>X-Forwarded-Port</code> header is found, otherwise port of the Java container.
     */
    protected  int getRequestPort(){
        String port = header("X-Forwarded-Port");
        return org.javalite.common.Util.blank(port)? port(): Integer.parseInt(port);
    }



    /**
     * Returns port on which the of the server received current request.
     *
     * @return port on which the of the server received current request.
     */
    protected  int port(){
        return RequestUtils.port();
    }


    /**
     * Returns protocol of request, for example: HTTP/1.1.
     *
     * @return protocol of request
     */
    protected  String protocol(){
        return RequestUtils.protocol();
    }

    //TODO: provide methods for: X-Forwarded-Proto and X-Forwarded-Port
    /**
     * This method returns a host name of a web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Host</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #host()} method is used.
     *
     * @return host name of web server if <code>X-Forwarded-Host</code> header is found, otherwise local host name.
     */
    protected  String getRequestHost() {
        return RequestUtils.getRequestHost();
    }

    /**
     * Returns IP address that the web server forwarded request for.
     *
     * @return IP address that the web server forwarded request for.
     */
    protected  String ipForwardedFor() {
        return RequestUtils.ipForwardedFor();
    }


    /**
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @return multiple request values for a name.
     */
    protected  List<String> params(String name){
        return RequestUtils.params(name);
    }


    /**
     * Returns a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     *
     * @return a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     */
    protected  Map<String, String> params1st(){
        return RequestUtils.params1st();
    }


    /**
     * Returns an instance of <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     *
     * @return an instance <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     */
    protected  Map<String, String[]> params(){
        return RequestUtils.params();
    }



    /**
     * Returns locale of request.
     *
     * @return locale of request.
     */
    protected  Locale locale(){
        return RequestUtils.locale();
    }

    /**
     * Same as {@link #locale()}.
     *
     * @return locale of request.
     */
    protected  Locale getLocale(){
        return RequestUtils.getLocale();
    }

    /**
     * Returns reference to a current session map.
     *
     * @return reference to a current session map.
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

    private SimpleHash getSessionHash(){
        return (SimpleHash)get("session");
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
    protected  Object sessionObject(String name){
        return session(name);
    }


    /**
     * Synonym of {@link #sessionObject(String)}.
     *
     * @param name name of session attribute
     * @return value of session attribute of null if not found
     */
    protected  Object session(String name){
        return session().get(name);
    }

    /**
     * Returns collection of all cookies browser sent.
     *
     * @return collection of all cookies browser sent.
     */
    public  List<Cookie> cookies(){
        return RequestUtils.cookies();
    }

    /**
     * Returns a cookie by name, null if not found.
     *
     * @param name name of a cookie.
     * @return a cookie by name, null if not found.
     */
    public  Cookie cookie(String name){
        return RequestUtils.cookie(name);
    }


    /**
     * Convenience method, returns cookie value.
     *
     * @param name name of cookie.
     * @return cookie value.
     */
    protected  String cookieValue(String name){
        return RequestUtils.cookieValue(name);
    }


    /**
     * Returns a path of the request. It does not include protocol, host, port or context. Just a path.
     * Example: <code>/controller/action/id</code>
     *
     * @return a path of the request.
     */
    protected  String path(){
        return RequestUtils.path();
    }

    /**
     * Returns a full URL of the request, all except a query string.
     *
     * @return a full URL of the request, all except a query string.
     */
    protected   String url(){
        return RequestUtils.url();
    }

    /**
     * Returns query string of the request.
     *
     * @return query string of the request.
     */
    protected   String queryString(){
        return RequestUtils.queryString();
    }

    /**
     * Returns an HTTP method from the request.
     *
     * @return an HTTP method from the request.
     */
    protected  String method(){
        return RequestUtils.method();
    }

    /**
     * True if this request uses HTTP GET method, false otherwise.
     *
     * @return True if this request uses HTTP GET method, false otherwise.
     */
    protected  boolean isGet() {
        return RequestUtils.isGet();
    }


    /**
     * True if this request uses HTTP POST method, false otherwise.
     *
     * @return True if this request uses HTTP POST method, false otherwise.
     */
    protected  boolean isPost() {
        return RequestUtils.isPost();
    }


    /**
     * True if this request uses HTTP PUT method, false otherwise.
     *
     * @return True if this request uses HTTP PUT method, false otherwise.
     */
    protected  boolean isPut() {
        return RequestUtils.isPut();
    }


    /**
     * True if this request uses HTTP DELETE method, false otherwise.
     *
     * @return True if this request uses HTTP DELETE method, false otherwise.
     */
    protected  boolean isDelete() {
        return RequestUtils.isDelete();
    }


    protected  boolean isMethod(String method){
        return RequestUtils.isMethod(method);
    }


    /**
     * True if this request uses HTTP HEAD method, false otherwise.
     *
     * @return True if this request uses HTTP HEAD method, false otherwise.
     */
    protected  boolean isHead() {
        return isMethod("head");
    }

    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
    protected  String context(){
        return RequestUtils.context();
    }

    /**
     * Returns URI, or a full path of request. This does not include protocol, host or port. Just context and path.
     * Examlpe: <code>/mywebapp/controller/action/id</code>
     * @return  URI, or a full path of request.
     */
    protected  String uri(){
        return RequestUtils.uri();
    }

    /**
     * Host name of the requesting client.
     *
     * @return host name of the requesting client.
     */
    protected  String remoteHost(){
        return RequestUtils.remoteHost();
    }

    /**
     * IP address of the requesting client.
     *
     * @return IP address of the requesting client.
     */
    protected  String remoteAddress(){
        return RequestUtils.remoteAddress();
    }



    /**
     * Returns a request header by name.
     *
     * @param name name of header
     * @return header value.
     */
    protected  String header(String name){
        return RequestUtils.header(name);
    }

    /**
     * Returns all headers from a request keyed by header name.
     *
     * @return all headers from a request keyed by header name.
     */
    protected  Map<String, String> headers(){
        return RequestUtils.headers();
    }
}

