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


import javalite.common.Util;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;
import static javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class HttpSupport {


    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected void logInfo(String info){
        logger.info(info);
    }

    protected void logDebug(String info){
        logger.debug(info);
    }

    protected void logWarning(String info){
        logger.warn(info);
    }

    protected void logWarning(String info, Throwable e){
        logger.warn(info, e);
    }

    protected void logError(String info){
        logger.error(info);
    }

    protected void logError(String info, Throwable e){
        logger.error(info, e);
    }


    /**
     * Assigns a value for a view.
     *
     * @param name name of value
     * @param value value.
     */
    protected void assign(String name, Object value) {
        if(value == null)
            throw new IllegalArgumentException("value '" + name + "' is null");

        KeyWords.check(name);
        ContextAccess.getHttpRequest().setAttribute(name, value);
    }

    /**
     * Alias to {@link #assign(String, Object)}.
     *
     * @param name name of object to be passed to view
     * @param value object to be passed to view
     */
    protected void view(String name, Object value) {
        assign(name, value);
    }

    public class HttpBuilder {
        private ControllerResponse controllerResponse;
        private HttpBuilder(ControllerResponse controllerResponse){
            this.controllerResponse = controllerResponse;
        }

        protected ControllerResponse getControllerResponse() {
            return controllerResponse;
        }

        /**
         * Sets content type of response.
         * These can be "text/html". Value "text/html" is set by default.
         *
         * @param contentType content type value.
         * @return HTTP Object
         */
        public HttpBuilder contentType(String contentType) {
            controllerResponse.setContentType(contentType);
            return this;
        }

        /**
         * Sets a HTTP header on response.
         *
         * @param name name of header.
         * @param value value of header.
         */
        public HttpBuilder header(String name, String value){
            ContextAccess.getHttpResponse().setHeader(name, value);
            return this;
        }

        /**
         * Overrides HTTP status with a different value.
         * For values and more information, look here:
         * <a href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP Status Codes</a>.
         *
         * By default, the status is set to 200, OK.
         *
         * @param status HTTP status code.
         */
        public void status(int status){
            controllerResponse.setStatus(status);
        }
    }

    public class RenderBuilder extends HttpBuilder {


        private RenderBuilder(RenderTemplateResponse response){
            super(response);
        }

        /**
         * Use this method to override a default layout configured.
         *
         * @param layout name of another layout.
         */
        public RenderBuilder layout(String layout){
            getRenderTemplateResponse().setLayout(layout);
            return this;
        }

        protected RenderTemplateResponse getRenderTemplateResponse(){
            return (RenderTemplateResponse)getControllerResponse();
        }

        /**
         * call this method to turn off all layouts. The view will be rendered raw - no layouts.
         */
        public RenderBuilder noLayout(){
            getRenderTemplateResponse().setLayout(null);
            return this;
        }
    }



    /**
     * Renders results with a template.
     *
     * This call must be the last call in the action.
     *
     * @param template - template name, must be "absolute", starting with slash,
     * such as: "/controller_dir/action_template".
     * @param values map with values to pass to view. 
     * @return instance of {@link RenderBuilder}, which is used to provide additional parameters.
     */
    protected RenderBuilder render(String template, Map values) {
        RenderTemplateResponse resp = new RenderTemplateResponse(values, template);
        ContextAccess.setControllerResponse(resp);
        return new RenderBuilder(resp);
    }


    /**
     * Redirects to a an action of this controller, or an action of a different controller.
     * This method does not expect a full URL.
     *
     * @param path - expected to be a path within the application.
     * @return instance of {@link activeweb.HttpSupport.HttpBuilder} to accept additional information.
     */
    protected HttpBuilder redirect(String path) {
        RedirectResponse resp = new RedirectResponse(path);
        ContextAccess.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }

    /**
     * Redirects to another URL (usually another site).
     *
     * @param url absolute URL: <code>http://domain/path...</code>.
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder redirect(URL url) {
        RedirectResponse resp = new RedirectResponse(url);
        ContextAccess.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }


    /**
     * Redirects to referrer if one exists. If a referrer does not exist, it will be redirected to
     * the <code>defaultReference</code>.
     *
     * @param defaultReference where to redirect - can be absolute or relative; this will be used in case
     * the request does not provide a "Referrer" header.
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder redirectToReferrer(String defaultReference) {
        String referrer = ContextAccess.getHttpRequest().getHeader("Referer");
        referrer = referrer == null? defaultReference: referrer;
        RedirectResponse resp = new RedirectResponse(referrer);
        ContextAccess.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }


    /**
     * Redirects to referrer if one exists. If a referrer does not exist, it will be redirected to
     * the root of the application.
     *
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder redirectToReferrer() {
        String referrer = ContextAccess.getHttpRequest().getHeader("Referer");
        referrer = referrer == null? ContextAccess.getHttpRequest().getContextPath(): referrer;
        RedirectResponse resp = new RedirectResponse(referrer);
        ContextAccess.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }


    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param action action to redirect to.
     * @param id id to redirect to.
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> HttpBuilder redirect(Class<T> controllerClass, String action, Object id){
        return redirect(controllerClass, map("action", action, "id", id));
    }

    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param id id to redirect to.
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> HttpBuilder redirect(Class<T> controllerClass, Object id){
        return redirect(controllerClass, map("id", id));
    }

    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param action action to redirect to.
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> HttpBuilder redirect(Class<T> controllerClass, String action){
        return redirect(controllerClass, map("action", action));
    }

    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> HttpBuilder redirect(Class<T> controllerClass){
        return redirect(controllerClass, new HashMap());

    }

    /**
     * Redirects to a controller, generates appropriate redirect path. There are two keyword keys expected in
     * the params map: "action" and "id". Both are optional. This method will generate appropriate URLs for regular as
     * well as RESTful controllers. The "action" and "id" values in the map will be treated as parts of URI such as:
     * <pre>
     * <code>
     * /controller/action/id
     * </code>
     * </pre>
     * for regular controllers, and:
     * <pre>
     * <code>
     * /controller/id/action
     * </code>
     * </pre>
     * for RESTful controllers. For RESTful controllers, the action names are limited to those described in
     * {@link activeweb.annotations.RESTful} and allowed on a GET URLs, which are: "edit_form" and "new_form".
     *
     * <p/>
     * The map may contain any number of other key/value pairs, which will be converted to a query string for
     * the redirect URI. Example:
     * <p/>
     * Method:
     * <pre>
     * <code>
     * redirect(app.controllers.PersonController.class,  javalite.collections.map("action", "show", "id", 123, "format", "json", "restrict", "true"));
     * </code>
     * </pre>
     * will generate the following URI:
     * <pre>
     * <code>
     * /person/show/123?format=json&restrict=true
     * </code>
     * </pre>
     *
     * This method will also perform URL - encoding of special characters if necessary.
     *
     *
     * @param controllerClass controller class
     * @param params map with request parameters.
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> HttpBuilder redirect(Class<T> controllerClass, Map params){
        String controllerPath = Router.getControllerPath(controllerClass);
        String contextPath = ContextAccess.getHttpRequest().getContextPath();
        String action = params.get("action") != null? params.get("action").toString() : null;
        String id = params.get("id") != null? params.get("id").toString() : null;
        boolean restful= AppController.restful(controllerClass);
        params.remove("action");
        params.remove("id");

        String uri = contextPath + Router.generate(controllerPath, action, id, restful, params);

        RedirectResponse resp = new RedirectResponse(uri);
        ContextAccess.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }

    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
     * and to support AJAX.
     *
     * @param text text of response.
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder respond(String text){
        DirectResponse resp = new DirectResponse(text);
        ContextAccess.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }

    /**
     * Convenience method for downloading files. This method will force the browser to find a handler(external program)
     *  for  this file (content type) and will provide a name of file to the browser. This method sets an HTTP header
     * "Content-Disposition" based on a file name.
     *
     * @param file file to download.
     * @return builder instance.
     * @throws FileNotFoundException thrown if file not found.
     */
    protected HttpBuilder sendFile(File file) throws FileNotFoundException {
        try{
            StreamResponse resp = new StreamResponse(new FileInputStream(file));
            ContextAccess.setControllerResponse(resp);
            HttpBuilder builder = new HttpBuilder(resp);
            builder.header("Content-Disposition", "attachment; filename=" + file.getName());
            return builder;
        }catch(Exception e){
            throw new ControllerException(e);
        }
    }

    /**
     * Returns value of one named parameter from request. If this name represents multiple values, this
     * call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of request parameter.
     */
    protected String param(String name){
        return name.equals("id") ? getId():ContextAccess.getHttpRequest().getParameter(name);
    }

    /**
     * Returns local host name on which request was received.
     * 
     * @return local host name on which request was received.
     */
    protected String host() {
        return ContextAccess.getHttpRequest().getLocalName();
    }


/**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     */
    protected String ipAddress() {
        return ContextAccess.getHttpRequest().getLocalAddr();
    }



    /**
     * This method returns a protocol of a request to web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Proto</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #protocol()} method is used.
     *
     * @return protocol of web server request if <code>X-Forwarded-Proto</code> header is found, otherwise current
     * protocol.
     */
    protected String getRequestProtocol(){
        String protocol = header("X-Forwarded-Proto");
        return Util.blank(protocol)? protocol(): protocol;
    }

    /**
     * This method returns a port of a web server if this Java container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Port</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #port()} method is used.
     *
     * @return port of web server request if <code>X-Forwarded-Port</code> header is found, otherwise port of the Java container.
     */
    protected int getRequestPort(){
        String port = header("X-Forwarded-Port");
        return Util.blank(port)? port(): Integer.parseInt(port);
    }



    /**
     * Returns port on which the of the server received current request.
     *
     * @return port on which the of the server received current request.
     */
    protected int port(){
        return ContextAccess.getHttpRequest().getLocalPort();
    }


    /**
     * Returns protocol of request, for example: HTTP/1.1.
     *
     * @return protocol of request
     */
    protected String protocol(){
        return ContextAccess.getHttpRequest().getProtocol();
    }

    //TODO: provide methods for: X-Forwarded-Proto and X-Forwarded-Port
    /**
     * This method returns a host name of a web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Host</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #host()} method is used. 
     *
     * @return host name of web server if <code>X-Forwarded-Host</code> header is found, otherwise local host name.
     */
    protected String getRequestHost() {
        String forwarded = header("X-Forwarded-Host");
        if (Util.blank(forwarded)) {
            return host();
        }
        String[] forwards = forwarded.split(",");
        return forwards[0].trim();
    }

    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    protected String getId(){
        String paramId = ContextAccess.getHttpRequest().getParameter("id");
        if(paramId != null && ContextAccess.getHttpRequest().getAttribute("id") != null){
            logger.warn("WARNING: probably you have 'id' supplied both as a HTTP parameter, as well as in the URI. Choosing parameter over URI value.");
        }

        String theId;
        if(paramId != null){
            theId =  paramId;
        }else{
            Object id = ContextAccess.getHttpRequest().getAttribute("id");
            theId =  id != null ? id.toString() : null;
        }
        return Util.blank(theId) ? null : theId;
    }


    /**
     * Returns a collection of uploaded files from a multi-part port request.
     *
     * @return a collection of uploaded files from a multi-part port request.
     */
    protected Iterator<FormItem> uploadedFiles() {
        HttpServletRequest req = ContextAccess.getHttpRequest();

        Iterator<FormItem> iterator;

        if(req instanceof AWMockMultipartHttpServletRequest){//running inside a test, and simulating upload.
            iterator = ((AWMockMultipartHttpServletRequest)req).getFormItemIterator();
        }else{
            if (!ServletFileUpload.isMultipartContent(req))
                throw new ControllerException("this is not a multipart request, be sure to add this attribute to the form: ... enctype=\"multipart/form-data\" ...");

            ServletFileUpload upload = new ServletFileUpload();
            try {
                FileItemIterator it = upload.getItemIterator(ContextAccess.getHttpRequest());
                iterator = new FormItemIterator(it);
            } catch (Exception e) {
                throw new ControllerException(e);
            }
        }
        return iterator;
    }


    /**
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @return multiple request values for a name.
     */
    protected List<String> params(String name){
        return name.equals("id") ? asList(getId()):asList(ContextAccess.getHttpRequest().getParameterValues(name));
    }

    /**
     * Returns an immutable java.util.Map containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     *
     * @return an immutable java.util.Map containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     */
    protected Map<String, String[]> params(){
        SimpleHash params = new SimpleHash(ContextAccess.getHttpRequest().getParameterMap());
        if(getId() != null)
            params.put("id", new String[]{getId()});
        return params;
    }


    /**
     * Returns a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     *
     * @return a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     */
    protected Map<String, String> params1st(){
        //TODO: candidate for performance optimization
        Map<String, String> params = new HashMap<String, String>();
        Enumeration names = ContextAccess.getHttpRequest().getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement().toString();
            params.put(name, ContextAccess.getHttpRequest().getParameter(name));
        }
        if(getId() != null)
            params.put("id", getId());
        return params;
    }

    /**
     * Returns reference to a current session. Creates a new session of one does not exist.
     * @return reference to a current session.
     */
    protected SessionFacade session(){
        return new SessionFacade();
    }

    /**
     * Returns collection of all cookies browser sent.
     *
     * @return collection of all cookies browser sent.
     */
    public List<Cookie> cookies(){
        javax.servlet.http.Cookie[] servletCookies = ContextAccess.getHttpRequest().getCookies();
        List<activeweb.Cookie> cookies = new ArrayList<Cookie>();
        for (javax.servlet.http.Cookie servletCookie: servletCookies) {
            activeweb.Cookie cookie = activeweb.Cookie.fromServletCookie(servletCookie);
            cookies.add(cookie);
        }
        return cookies;
    }

    /**
     * Returns a cookie by name, null if not found.
     *
     * @param name name of a cookie.
     * @return a cookie by name, null if not found.
     */
    public Cookie cookie(String name){
        javax.servlet.http.Cookie[] servletCookies = ContextAccess.getHttpRequest().getCookies();
        if (servletCookies != null) {
            for (javax.servlet.http.Cookie servletCookie : servletCookies) {
                if (servletCookie.getName().equals(name)) {
                    return activeweb.Cookie.fromServletCookie(servletCookie);
                }
            }
        }
        return null;
    }


    /**
     * Convenience method, returns cookie value.
     *
     * @param name name of cookie.
     * @return cookie value.
     */
    protected String cookieValue(String name){
        return cookie(name).getValue();
    }

    /**
     * Sends cookie to browse with response.
     *
     * @param cookie cookie to send.
     */
    public void sendCookie(Cookie cookie){
        ContextAccess.getHttpResponse().addCookie(activeweb.Cookie.toServletCookie(cookie));
    }

    /**
     * Sends cookie to browse with response.
     *
     * @param name name of cookie
     * @param value value of cookie.
     */
    public void sendCookie(String name, String value) {
        ContextAccess.getHttpResponse().addCookie(activeweb.Cookie.toServletCookie(new Cookie(name, value)));
    }


    /**
     * Sends long to live cookie to browse with response. This cookie will be asked to live for 20 years.
     *
     * @param name name of cookie
     * @param value value of cookie.
     */
    public void sendPermanentCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(60*60*24*365*20);
        ContextAccess.getHttpResponse().addCookie(activeweb.Cookie.toServletCookie(cookie));
    }

    /**
     * Returns a path of the request. It does not include protocol, host, port or context. Just a path.
     * Example: <code>/controller/action/id</code>
     *
     * @return a path of the request.
     */
    protected String path(){
        return ContextAccess.getHttpRequest().getServletPath();
    }

    /**
     * Returns a full URL of the request, all except a query string.
     *
     * @return a full URL of the request, all except a query string.
     */
    protected  String url(){
        return ContextAccess.getHttpRequest().getRequestURL().toString();
    }

    /**
     * Returns query string of the request.
     *
     * @return query string of the request.
     */
    protected  String queryString(){
        return ContextAccess.getHttpRequest().getQueryString();
    }

    /**
     * Returns an HTTP method from the request.
     *
     * @return an HTTP method from the request.
     */
    protected String method(){
        return ContextAccess.getHttpRequest().getMethod();
    }

    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
    protected String context(){
        return ContextAccess.getHttpRequest().getContextPath();
    }


    /**
     * Returns URI, or a full path of request. This does not include protocol, host or port. Just context and path.
     * Examlpe: <code>/mywebapp/controller/action/id</code>
     * @return  URI, or a full path of request.
     */
    protected String uri(){
        return ContextAccess.getHttpRequest().getRequestURI();
    }

    /**
     * Host name of the requesting client.
     *
     * @return host name of the requesting client.
     */
    protected String remoteHost(){
        return ContextAccess.getHttpRequest().getRemoteHost();
    }

    /**
     * IP address of the requesting client.
     *
     * @return IP address of the requesting client.
     */
    protected String remoteAddress(){
        return ContextAccess.getHttpRequest().getRemoteAddr();
    }



    /**
     * Returns a request header by name.
     *
     * @param name name of header
     * @return header value.
     */
    protected String header(String name){
        return ContextAccess.getHttpRequest().getHeader(name);
    }

    /**
     * Returns all headers from a request keyed by header name.
     *
     * @return all headers from a request keyed by header name.
     */
    public Map<String, String> headers(){

        Map<String, String> headers = new HashMap<String, String>();
        Enumeration<String> names = ContextAccess.getHttpRequest().getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, ContextAccess.getHttpRequest().getHeader(name));
        }
        return headers;
    }

    /**
     * Adds a header to response.
     *
     * @param name name of header.
     * @param value value of header.
     */
    protected void header(String name, String value){
        ContextAccess.getHttpResponse().addHeader(name, value);
    }

    /**
     * Streams content of the <code>reader</code> to the HTTP client.
     *
     * @param in input stream to read bytes from.
     * @return {@link activeweb.HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder streamOut(InputStream in) {
        StreamResponse resp = new StreamResponse(in);
        ContextAccess.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }


    /**
     * Returns a String containing the real path for a given virtual path. For example, the path "/index.html" returns
     * the absolute file path on the server's filesystem would be served by a request for
     * "http://host/contextPath/index.html", where contextPath is the context path of this ServletContext..
     * <p/>
     * The real path returned will be in a form appropriate to the computer and operating system on which the servlet
     * <p/>
     * container is running, including the proper path separators. This method returns null if the servlet container
     * cannot translate the virtual path to a real path for any reason (such as when the content is being made
     * available from a .war archive).
     *
     * <p/>
     * JavaDoc copied from: <a href="http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29">
     * http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29</a>
     *
     * @param path a String specifying a virtual path
     * @return a String specifying the real path, or null if the translation cannot be performed
     */
    protected String getRealPath(String path) {
        return ContextAccess.getFilterConfig().getServletContext().getRealPath(path);
    }

    /**
     * Use to send raw data to HTTP client. Content type and headers will not be set.
     * Response code will be set to 200.
     *
     * @return instance of output stream to send raw data directly to HTTP client.
     */
    protected OutputStream outputStream(){
        return outputStream(null, null, 200);
    }

    /**
     * Use to send raw data to HTTP client. Status will be set to 200.
     *
     * @param contentType content type
     * @return instance of output stream to send raw data directly to HTTP client.
     */
    protected OutputStream outputStream(String contentType) {
        return outputStream(contentType, null, 200);
    }


    /**
     * Use to send raw data to HTTP client.
     *
     * @param contentType content type
     * @param headers set of headers.
     * @param status status.
     * @return instance of output stream to send raw data directly to HTTP client.
     */
    protected OutputStream outputStream(String contentType, Map headers, int status) {
        try {
            ContextAccess.setControllerResponse(new NopResponse(contentType, status));

            if (headers != null) {
                for (Object key : headers.keySet()) {
                    if (headers.get(key) != null)
                        ContextAccess.getHttpResponse().addHeader(key.toString(), headers.get(key).toString());
                }
            }

            return ContextAccess.getHttpResponse().getOutputStream();
        }catch(Exception e){
            throw new ControllerException(e);
        }
    }

    /**
     * Produces a writer for sending raw data to HTTP clients.
     *
     * Content type content type not be set on the response. Headers will not be send to client. Status will be
     * set to 200.
     * @return instance of a writer for writing content to HTTP client.
     */
    protected PrintWriter writer(){
        return writer(null, null, 200);
    }

    /**
     * Produces a writer for sending raw data to HTTP clients.
     *
     * @param contentType content type. If null - will not be set on the response
     * @param headers headers. If null - will not be set on the response
     * @param status will be sent to browser.
     * @return instance of a writer for writing content to HTTP client.
     */
    protected PrintWriter writer(String contentType, Map headers, int status){
        try{
            ContextAccess.setControllerResponse(new NopResponse(contentType, status));

            if (headers != null) {
                for (Object key : headers.keySet()) {
                    if (headers.get(key) != null)
                        ContextAccess.getHttpResponse().addHeader(key.toString(), headers.get(key).toString());
                }
            }

            return ContextAccess.getHttpResponse().getWriter();
        }catch(Exception e){
            throw new ControllerException(e);
        }
    }

    /**
     * Returns true if any named request parameter is blank.
     *
     * @param names names of request parameters.
     * @return true if any request parameter is blank.
     */
    protected boolean blank(String ... names){
        //TODO: write test, move elsewhere - some helper
        for(String name:names){
            if(Util.blank(param(name))){
                return true;
            }
        }
        return false;
    }


    /**
     * Returns true if this request is Ajax.
     *
     * @return true if this request is Ajax.
     */
    protected boolean isXhr(){
        return header("X-Requested-With") != null || header("x-requested-with") != null;
    }

    /**
     * Synonym for {@link #isXhr()}.
     */
    protected boolean xhr(){
        return isXhr();
    }

    /**
     * Returns instance of {@link activeweb.AppContext}.
     *
     * @return
     */
    protected AppContext appContext(){
        return ContextAccess.getAppContext();
    }
}
