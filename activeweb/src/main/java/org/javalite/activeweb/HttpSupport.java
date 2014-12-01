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


import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.javalite.common.Convert;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class HttpSupport {
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    private List<FormItem> formItems;

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

    protected void logError(Throwable e){
        logger.error("", e);
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
        KeyWords.check(name);
        Context.getValues().put(name, value);
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


    /**
     * Convenience method, calls {@link #assign(String, Object)} internally.
     * The keys in teh map are converted to String values.
     *
     * @param values map with values to pass to view.
     */
    protected void view(Map values){
        for(Object key:values.keySet() ){
            assign(key.toString(), values.get(key));
        }
    }

    /**
     * Convenience method to pass multiple names and corresponding values to a view.
     *
     * @param values - pairs of names and values. such as: name1, value1, name2, value2, etc. Number of arguments must be even.
     */
    protected void view(Object ... values){
        view(map(values));
    }

    /**
     * Flash method to display multiple flash messages.
     * Takes in a map of names and values for a flash.
     * Keys act like names, and values act like... ehr.. values.
     *
     * @see #flash(String, Object)
     *
     * @param values values to flash.
     */
    protected void flash(Map values){
        for(Object key:values.keySet() ){
            flash(key.toString(), values.get(key));
        }
    }

    /**
     * Flash method to display multiple flash messages.
     * Takes in a vararg of values for flash. Number of arguments must be even.
     * Format: name, value, name, value, etc.
     *
     * @see #flash(String, Object)
     * @param values values to flash.
     */
    protected void flash(Object ... values){
        flash(map(values));
    }

    /**
     * Sets a flash name for a flash with  a body.
     * Here is a how to use a tag with a body:
     *
     * <pre>
     * &lt;@flash name=&quot;warning&quot;&gt;
         &lt;div class=&quot;warning&quot;&gt;${message}&lt;/div&gt;
       &lt;/@flash&gt;
     * </pre>
     *
     * If body refers to variables (as in this example), then such variables need to be passed in to the template as usual using
     * the {@link #view(String, Object)} method.
     *
     * @param name name of a flash
     */
    @SuppressWarnings("unchecked")
    protected void flash(String name){
        flash(name, null);
    }

    /**
     * Sends value to flash. Flash survives one more request.  Using flash is typical
     * for POST/GET pattern,
     *
     * @param name name of value to flash
     * @param value value to live for one more request in current session.
     */
    @SuppressWarnings("unchecked")
    protected void flash(String name, Object value) {
        if (session().get("flasher") == null) {
            session().put("flasher", new HashMap());
        }
        ((Map) session().get("flasher")).put(name, value);
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
         * @return instance of RenderBuilder
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
         * @return instance of RenderBuilder
         */
        public HttpBuilder header(String name, String value){
            Context.getHttpResponse().setHeader(name, value);
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
         * @return instance of RenderBuilder
         */
        public RenderBuilder layout(String layout){
            getRenderTemplateResponse().setLayout(layout);
            return this;
        }

        protected RenderTemplateResponse getRenderTemplateResponse(){
            return (RenderTemplateResponse)getControllerResponse();
        }

        /**
         * Call this method to turn off all layouts. The view will be rendered raw - no layouts.
         * @return instance of RenderBuilder
         */
        public RenderBuilder noLayout(){
            getRenderTemplateResponse().setLayout(null);
            return this;
        }

        /**
         * Sets a format for the current request. This is a intermediate extension for the template file. For instance,
         * if the name of template file is document.xml.ftl, then the "xml" part is set with this method, the
         * "document" is a template name, and "ftl" extension is assumed in case you use FreeMarker template manager.
         *
         * @param format template format
         * @return instance of RenderBuilder
         */
        public RenderBuilder format(String format){
            ControllerResponse response = Context.getControllerResponse();
            if(response instanceof RenderTemplateResponse){
                ((RenderTemplateResponse)response).setFormat(format);
            }
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
        RenderTemplateResponse resp = new RenderTemplateResponse(values, template, Context.getFormat());
        Context.setControllerResponse(resp);
        return new RenderBuilder(resp);
    }


    /**
     * Redirects to a an action of this controller, or an action of a different controller.
     * This method does not expect a full URL.
     *
     * @param path - expected to be a path within the application.
     * @return instance of {@link HttpSupport.HttpBuilder} to accept additional information.
     */
    protected HttpBuilder redirect(String path) {
        RedirectResponse resp = new RedirectResponse(path);
        Context.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }

    /**
     * Redirects to another URL (usually another site).
     *
     * @param url absolute URL: <code>http://domain/path...</code>.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder redirect(URL url) {
        RedirectResponse resp = new RedirectResponse(url);
        Context.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }


    /**
     * Redirects to referrer if one exists. If a referrer does not exist, it will be redirected to
     * the <code>defaultReference</code>.
     *
     * @param defaultReference where to redirect - can be absolute or relative; this will be used in case
     * the request does not provide a "Referrer" header.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder redirectToReferrer(String defaultReference) {
        String referrer = Context.getHttpRequest().getHeader("Referer");
        referrer = referrer == null? defaultReference: referrer;
        RedirectResponse resp = new RedirectResponse(referrer);
        Context.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }


    /**
     * Redirects to referrer if one exists. If a referrer does not exist, it will be redirected to
     * the root of the application.
     *
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder redirectToReferrer() {
        String referrer = Context.getHttpRequest().getHeader("Referer");
        referrer = referrer == null? Context.getHttpRequest().getContextPath(): referrer;
        RedirectResponse resp = new RedirectResponse(referrer);
        Context.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }


    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param action action to redirect to.
     * @param id id to redirect to.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> HttpBuilder redirect(Class<T> controllerClass, String action, Object id){
        return redirect(controllerClass, map("action", action, "id", id));
    }

    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param id id to redirect to.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> HttpBuilder redirect(Class<T> controllerClass, Object id){
        return redirect(controllerClass, map("id", id));
    }

    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param action action to redirect to.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> HttpBuilder redirect(Class<T> controllerClass, String action){
        return redirect(controllerClass, map("action", action));
    }

    /**
     * Redirects to the same controller, and action "index". This is equivalent to
     * <pre>
     *     redirect(BooksController.class);
     * </pre>
     * given that the current controller is <code>BooksController</code>.
     *
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder redirect() {
        return redirect(getRoute().getController().getClass());
    }

    /**
     * Redirects to given controller, action "index" without any parameters.
     *
     * @param controllerClass controller class where to send redirect.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
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
     * {@link org.javalite.activeweb.annotations.RESTful} and allowed on a GET URLs, which are: "edit_form" and "new_form".
     *
     * <p/>
     * The map may contain any number of other key/value pairs, which will be converted to a query string for
     * the redirect URI. Example:
     * <p/>
     * Method:
     * <pre>
     * <code>
     * redirect(app.controllers.PersonController.class,  org.javalite.common.Collections.map("action", "show", "id", 123, "format", "json", "restrict", "true"));
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
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> HttpBuilder redirect(Class<T> controllerClass, Map params){
        String controllerPath = Router.getControllerPath(controllerClass);
        String contextPath = Context.getHttpRequest().getContextPath();
        String action = params.get("action") != null? params.get("action").toString() : null;
        String id = params.get("id") != null? params.get("id").toString() : null;
        boolean restful= AppController.restful(controllerClass);
        params.remove("action");
        params.remove("id");

        String uri = contextPath + Router.generate(controllerPath, action, id, restful, params);

        RedirectResponse resp = new RedirectResponse(uri);
        Context.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }

    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
     * and to support AJAX.
     *
     * @param text text of response.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder respond(String text){
        DirectResponse resp = new DirectResponse(text);
        Context.setControllerResponse(resp);
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
            Context.setControllerResponse(resp);
            HttpBuilder builder = new HttpBuilder(resp);
            builder.header("Content-Disposition", "attachment; filename=" + file.getName());
            return builder;
        }catch(Exception e){
            throw new ControllerException(e);
        }
    }


    /**
     * Convenience method to get file content from <code>multipart/form-data</code> request. If more than one files with the same
     * name are submitted, only one is returned.
     *
     * @param fieldName name of form field from the  <code>multipart/form-data</code> request corresponding to the uploaded file.
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return <code>InputStream</code> from which to read content of uploaded file or null if FileItem with this name is not found.
     */
    protected org.javalite.activeweb.FileItem getFile(String fieldName, List<FormItem> formItems){
        for (FormItem formItem : formItems) {
            if(formItem instanceof org.javalite.activeweb.FileItem && formItem.getFieldName().equals(fieldName)){
                return (org.javalite.activeweb.FileItem)formItem;
            }
        }
        return null;
    }

    /**
     * Returns value of one named parameter from request. If this name represents multiple values, this
     * call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of request parameter.
     * @see org.javalite.activeweb.RequestUtils#param(String)
     */
    protected String param(String name){
        return RequestUtils.param(name);
    }


    /**
     * Convenience method to get a parameter in case <code>multipart/form-data</code> request was used.
     *
     * Returns value of one named parameter from request. If this name represents multiple values, this
     * call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return value of request parameter.
     * @see org.javalite.activeweb.RequestUtils#param(String)
     */
    protected String param(String name, List<FormItem> formItems){
        return RequestUtils.param(name, formItems);
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
    protected String host() {
        return RequestUtils.host();
    }


    /**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     */
    protected String ipAddress() {
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
    protected String getRequestProtocol(){
        return RequestUtils.getRequestProtocol();
    }

    /**
     * This method returns a port of a web server if this Java container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Port</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #port()} method is used.
     *
     * @return port of web server request if <code>X-Forwarded-Port</code> header is found, otherwise port of the Java container.
     */
    protected int getRequestPort(){
        return RequestUtils.getRequestPort();
    }



    /**
     * Returns port on which the of the server received current request.
     *
     * @return port on which the of the server received current request.
     */
    protected int port(){
        return RequestUtils.port();
    }


    /**
     * Returns protocol of request, for example: HTTP/1.1.
     *
     * @return protocol of request
     */
    protected String protocol(){
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
    protected String getRequestHost() {
        return RequestUtils.getRequestHost();
    }

    /**
     * Returns IP address that the web server forwarded request for.
     *
     * @return IP address that the web server forwarded request for.
     */
    protected String ipForwardedFor() {
        return RequestUtils.ipForwardedFor();
    }

    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    protected String getId(){
        return RequestUtils.getId();
    }


    /**
     * Returns a collection of uploaded files from a multi-part port request.
     * Uses request encoding if one provided, and sets no limit on the size of upload.
     *
     * @return a collection of uploaded files from a multi-part port request.
     */
    protected Iterator<FormItem> uploadedFiles() {
        return uploadedFiles(null, -1);
    }

    /**
     * Returns a collection of uploaded files from a multi-part port request.
     * Sets no limit on the size of upload.
     *
     * @param encoding specifies the character encoding to be used when reading the headers of individual part.
     * When not specified, or null, the request encoding is used. If that is also not specified, or null,
     * the platform default encoding is used.
     *
     * @return a collection of uploaded files from a multi-part port request.
     */
    protected Iterator<FormItem> uploadedFiles(String encoding) {
        return uploadedFiles(encoding, -1);
    }


    /**
     * Returns a collection of uploaded files from a multi-part port request.
     *
     * @param encoding specifies the character encoding to be used when reading the headers of individual part.
     * When not specified, or null, the request encoding is used. If that is also not specified, or null,
     * the platform default encoding is used.
     * @param maxFileSize maximum file size in the upload in bytes. -1 indicates no limit.
     *
     * @return a collection of uploaded files from a multi-part port request.
     */
    protected Iterator<FormItem> uploadedFiles(String encoding, long maxFileSize) {
        HttpServletRequest req = Context.getHttpRequest();

        Iterator<FormItem> iterator;

        if(req instanceof AWMockMultipartHttpServletRequest){//running inside a test, and simulating upload.
            iterator = ((AWMockMultipartHttpServletRequest)req).getFormItemIterator();
        }else{
            if (!ServletFileUpload.isMultipartContent(req))
                throw new ControllerException("this is not a multipart request, be sure to add this attribute to the form: ... enctype=\"multipart/form-data\" ...");

            ServletFileUpload upload = new ServletFileUpload();
            if(encoding != null)
                upload.setHeaderEncoding(encoding);
            upload.setFileSizeMax(maxFileSize);
            try {
                FileItemIterator it = upload.getItemIterator(Context.getHttpRequest());
                iterator = new FormItemIterator(it);
            } catch (Exception e) {
                throw new ControllerException(e);
            }
        }
        return iterator;
    }


    /**
     * Convenience method, calls {@link #multipartFormItems(String)}. Does not set encoding before reading request.
     * @see #multipartFormItems(String)
     * @return a collection of uploaded files/fields from a multi-part request.
     */
    protected List<FormItem> multipartFormItems() {
        return multipartFormItems(null);
    }


    /**
     * Returns a collection of uploaded files and form fields from a multi-part request.
     * This method uses <a href="http://commons.apache.org/proper/commons-fileupload/apidocs/org/apache/commons/fileupload/disk/DiskFileItemFactory.html">DiskFileItemFactory</a>.
     * As a result, it is recommended to add the following to your web.xml file:
     *
     * <pre>
     *   &lt;listener&gt;
     *      &lt;listener-class&gt;
     *         org.apache.commons.fileupload.servlet.FileCleanerCleanup
     *      &lt;/listener-class&gt;
     *   &lt;/listener&gt;
     *</pre>
     *
     * For more information, see: <a href="http://commons.apache.org/proper/commons-fileupload/using.html">Using FileUpload</a>
     *
     * The size of upload defaults to max of 20mb. Files greater than that will be rejected. If you want to accept files
     * smaller of larger, create a file called <code>activeweb.properties</code>, add it to your classpath and
     * place this property to the file:
     *
     * <pre>
     * #max upload size
     * maxUploadSize = 20000000
     * </pre>
     *
     * @param encoding specifies the character encoding to be used when reading the headers of individual part.
     * When not specified, or null, the request encoding is used. If that is also not specified, or null,
     * the platform default encoding is used.
     *
     * @return a collection of uploaded files from a multi-part request.
     */
    protected List<FormItem> multipartFormItems(String encoding) {
        //we are thread safe, because controllers are pinned to a thread and discarded after each request.
        if(formItems != null ){
            return formItems;
        }

        HttpServletRequest req = Context.getHttpRequest();

        if (req instanceof AWMockMultipartHttpServletRequest) {//running inside a test, and simulating upload.
            formItems = ((AWMockMultipartHttpServletRequest) req).getFormItems();
        } else {

            if (!ServletFileUpload.isMultipartContent(req))
                throw new ControllerException("this is not a multipart request, be sure to add this attribute to the form: ... enctype=\"multipart/form-data\" ...");

            DiskFileItemFactory factory = new DiskFileItemFactory();

            factory.setSizeThreshold(Configuration.getMaxUploadSize());
            factory.setRepository(Configuration.getTmpDir());

            ServletFileUpload upload = new ServletFileUpload(factory);
            if(encoding != null)
                upload.setHeaderEncoding(encoding);
            upload.setFileSizeMax(Configuration.getMaxUploadSize());
            try {
                List<org.apache.commons.fileupload.FileItem> apacheFileItems = upload.parseRequest(Context.getHttpRequest());
                formItems = new ArrayList<FormItem>();
                for (FileItem apacheItem : apacheFileItems) {
                    ApacheFileItemFacade f = new ApacheFileItemFacade(apacheItem);
                    if(f.isFormField()){
                        formItems.add(new FormItem(f));
                    }else{
                        formItems.add(new org.javalite.activeweb.FileItem(f));
                    }
                }
                return formItems;
            } catch (Exception e) {
                throw new ControllerException(e);
            }
        }
        return formItems;
    }


    /**
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @return multiple request values for a name.
     */
    protected List<String> params(String name){
        return RequestUtils.params(name);
    }

    /**
     * Returns an instance of <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     *
     * @return an instance <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     */
    protected Map<String, String[]> params(){
        return RequestUtils.params();
    }

    /**
     * Convenience method to get parameters in case <code>multipart/form-data</code> request was used.
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return multiple request values for a name.
     */
    protected List<String> params(String name, List<FormItem> formItems){
        return RequestUtils.params(name, formItems);
    }

    /**
     * Returns a map parsed from a request if parameter names have a "hash" syntax:
     *
     *  <pre>
     *  &lt;input type=&quot;text&quot; name=&quot;account[name]&quot; /&gt;
        &lt;input type=&quot;text&quot; name=&quot;account[number]&quot; /&gt;
     * </pre>
     *
     * will result in a map where keys are names of hash elements, and values are values of these elements from request.
     * For the example above, the map will have these values:
     *
     * <pre>
     *     { "name":"John", "number": "123" }
     * </pre>
     *
     * @param hashName - name of a hash. In the example above, it will be "account".
     * @return map with name/value pairs parsed from request.
     */
    public Map<String, String> getMap(String hashName) {
        Map<String, String[]>  params = params();
        Map<String, String>  hash = new HashMap<String, String>();
        for(String key:params.keySet()){
            if(key.startsWith(hashName)){
                String name = parseHashName(key);
                if(name != null){
                    hash.put(name, param(key));
                }
            }
        }
        return hash;
    }

    /**
     * Convenience method to get parameter map in case <code>multipart/form-data</code> request was used.
     * This method will skip files, and will only return form fields that are not files.
     *
     * Returns a map parsed from a request if parameter names have a "hash" syntax:
     *
     *  <pre>
     *  &lt;input type=&quot;text&quot; name=&quot;account[name]&quot; /&gt;
     *  &lt;input type=&quot;text&quot; name=&quot;account[number]&quot; /&gt;
     * </pre>
     *
     * will result in a map where keys are names of hash elements, and values are values of these elements from request.
     * For the example above, the map will have these values:
     *
     * <pre>
     *     { "name":"John", "number": "123" }
     * </pre>
     *
     * @param hashName - name of a hash. In the example above, it will be "account".
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return map with name/value pairs parsed from request.
     */
    public Map<String, String> getMap(String hashName, List<FormItem> formItems) {
        Map<String, String>  hash = new HashMap<String, String>();
        for(FormItem item:formItems){
            if(item.getFieldName().startsWith(hashName) && !item.isFile()){
                String name = parseHashName(item.getFieldName());
                if(name != null){
                    hash.put(name, item.getStreamAsString());
                }
            }
        }
        return hash;
    }

    private static Pattern hashPattern = Pattern.compile("\\[.*\\]");

    /**
     * Parses name from hash syntax.
     *
     * @param param something like this: <code>person[account]</code>
     * @return name of hash key:<code>account</code>
     */
    private static String parseHashName(String param) {
        Matcher matcher = hashPattern.matcher(param);
        String name = null;
        while (matcher.find()){
            name = matcher.group(0);
        }
        return name == null? null : name.substring(1, name.length() - 1);
    }


    public static void main(String[] args){
        System.out.println(HttpSupport.parseHashName("account[name]"));
    }

    /**
     * Sets character encoding for request. Has to be called before reading any parameters of getting input
     * stream.
     * @param encoding encoding to be set.
     *
     * @throws UnsupportedEncodingException
     */
    protected void setRequestEncoding(String encoding) throws UnsupportedEncodingException {
        Context.getHttpRequest().setCharacterEncoding(encoding);
    }


    /**
     * Sets character encoding for response.
     *
     * @param encoding encoding to be set.
     */
    protected void setResponseEncoding(String encoding) {
        Context.getHttpResponse().setCharacterEncoding(encoding);
    }



    /**
     * Sets character encoding on the response.
     *
     * @param encoding character encoding for response.
     */
    protected void setEncoding(String encoding){
        Context.setEncoding(encoding);
    }

    /**
     * Synonym for {@link #setEncoding(String)}
     *
     * @param encoding encoding of response to client
     */
    protected void encoding(String encoding){
        setEncoding(encoding);
    }

    /**
     * Controllers can override this method to return encoding they require. Encoding set in method {@link #setEncoding(String)}
     * trumps this setting.
     *
     * @return null. If this method is not overridden and encoding is not set from an action or filter,
     * encoding will be set according to container implementation.
     */
    protected String getEncoding(){
        return null;
    }

    /**
     * Sets content length of response.
     *
     * @param length content length of response.
     */
    protected void setContentLength(int length){
        Context.getHttpResponse().setContentLength(length);
    }
    /**
     * Sets locale on response.
     *
     * @param locale locale for response.
     */
    protected void setLocale(Locale locale){
        Context.getHttpResponse().setLocale(locale);
    }

    /**
     * Same as {@link #setLocale(java.util.Locale)}
     *
     * @param locale locale for response
     */
    protected void locale(Locale locale){
        Context.getHttpResponse().setLocale(locale);
    }

    /**
     * Returns locale of request.
     *
     * @return locale of request.
     */
    protected Locale locale(){
        return RequestUtils.locale();
    }

    /**
     * Same as {@link #locale()}.
     *
     * @return locale of request.
     */
    protected Locale getLocale(){
        return RequestUtils.getLocale();
    }


    /**
     * Returns a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     *
     * @return a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     */
    protected Map<String, String> params1st(){
        return RequestUtils.params1st();
    }


    /**
     * Convenience method to get first parameter values in case <code>multipart/form-data</code> request was used.
     * Returns a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     */
    protected Map<String, String> params1st(List<FormItem> formItems){
        return RequestUtils.params1st(formItems);
    }

    /**
     * Returns reference to a current session. Creates a new session of one does not exist.
     * @return reference to a current session.
     */
    protected SessionFacade session(){
        return new SessionFacade();
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
        return session(name);
    }

    /**
     * Synonym of {@link #sessionObject(String)}.
     *
     * @param name name of session attribute
     * @return value of session attribute of null if not found
     */
    protected Object session(String name){
        Object val = session().get(name);
        return val == null ? null : val;
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
        return Convert.toString(session(name));
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
        return Convert.toInteger(session(name));
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
        return Convert.toBoolean(session(name));
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
        return Convert.toDouble(session(name));
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
        return Convert.toFloat(session(name));
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
        return Convert.toLong(session(name));
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

    /**
     * Returns collection of all cookies browser sent.
     *
     * @return collection of all cookies browser sent.
     */
    public List<Cookie> cookies(){
        return RequestUtils.cookies();
    }

    /**
     * Returns a cookie by name, null if not found.
     *
     * @param name name of a cookie.
     * @return a cookie by name, null if not found.
     */
    public Cookie cookie(String name){
        return RequestUtils.cookie(name);
    }


    /**
     * Convenience method, returns cookie value.
     *
     * @param name name of cookie.
     * @return cookie value.
     */
    protected String cookieValue(String name){
        return RequestUtils.cookieValue(name);
    }

    /**
     * Sends cookie to browse with response.
     *
     * @param cookie cookie to send.
     */
    public void sendCookie(Cookie cookie){
        Context.getHttpResponse().addCookie(Cookie.toServletCookie(cookie));
    }

    /**
     * Sends cookie to browse with response.
     *
     * @param name name of cookie
     * @param value value of cookie.
     */
    public void sendCookie(String name, String value) {
        Context.getHttpResponse().addCookie(Cookie.toServletCookie(new Cookie(name, value)));
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
        Context.getHttpResponse().addCookie(Cookie.toServletCookie(cookie));
    }

    /**
     * Returns a path of the request. It does not include protocol, host, port or context. Just a path.
     * Example: <code>/controller/action/id</code>
     *
     * @return a path of the request.
     */
    protected String path(){
        return RequestUtils.path();

    }

    /**
     * Returns a full URL of the request, all except a query string.
     *
     * @return a full URL of the request, all except a query string.
     */
    protected  String url(){
        return RequestUtils.url();
    }

    /**
     * Returns query string of the request.
     *
     * @return query string of the request.
     */
    protected  String queryString(){
        return RequestUtils.queryString();
    }

    /**
     * Returns an HTTP method from the request.
     *
     * @return an HTTP method from the request.
     */
    protected String method(){
        return RequestUtils.method();
    }

    /**
     * True if this request uses HTTP GET method, false otherwise.
     *
     * @return True if this request uses HTTP GET method, false otherwise.
     */
    protected boolean isGet() {
        return RequestUtils.isGet();
    }


    /**
     * True if this request uses HTTP POST method, false otherwise.
     *
     * @return True if this request uses HTTP POST method, false otherwise.
     */
    protected boolean isPost() {
        return RequestUtils.isPost();
    }


    /**
     * True if this request uses HTTP PUT method, false otherwise.
     *
     * @return True if this request uses HTTP PUT method, false otherwise.
     */
    protected boolean isPut() {
        return RequestUtils.isPut();
    }


    /**
     * True if this request uses HTTP DELETE method, false otherwise.
     *
     * @return True if this request uses HTTP DELETE method, false otherwise.
     */
    protected boolean isDelete() {
        return RequestUtils.isDelete();
    }


    private boolean isMethod(String method){
        return RequestUtils.isMethod(method);
    }


    /**
     * True if this request uses HTTP HEAD method, false otherwise.
     *
     * @return True if this request uses HTTP HEAD method, false otherwise.
     */
    protected boolean isHead() {
        return RequestUtils.isHead();
    }

    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
    protected String context(){
        return RequestUtils.context();
    }


    /**
     * Returns URI, or a full path of request. This does not include protocol, host or port. Just context and path.
     * Examlpe: <code>/mywebapp/controller/action/id</code>
     * @return  URI, or a full path of request.
     */
    protected String uri(){
        return RequestUtils.uri();
    }

    /**
     * Host name of the requesting client.
     *
     * @return host name of the requesting client.
     */
    protected String remoteHost(){
        return RequestUtils.remoteHost();
    }

    /**
     * IP address of the requesting client.
     *
     * @return IP address of the requesting client.
     */
    protected String remoteAddress(){
        return RequestUtils.remoteAddress();
    }



    /**
     * Returns a request header by name.
     *
     * @param name name of header
     * @return header value.
     */
    protected String header(String name){
        return RequestUtils.header(name);
    }

    /**
     * Returns all headers from a request keyed by header name.
     *
     * @return all headers from a request keyed by header name.
     */
    protected Map<String, String> headers(){
        return RequestUtils.headers();
    }

    /**
     * Adds a header to response.
     *
     * @param name name of header.
     * @param value value of header.
     */
    protected void header(String name, String value){
        Context.getHttpResponse().addHeader(name, value);
    }

    /**
     * Adds a header to response.
     *
     * @param name name of header.
     * @param value value of header.
     */
    protected void header(String name, Object value){
        if(value == null) throw new NullPointerException("value cannot be null");

        header(name, value.toString());
    }

    /**
     * Streams content of the <code>reader</code> to the HTTP client.
     *
     * @param in input stream to read bytes from.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder streamOut(InputStream in) {
        StreamResponse resp = new StreamResponse(in);
        Context.setControllerResponse(resp);
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
        return Context.getFilterConfig().getServletContext().getRealPath(path);
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
            Context.setControllerResponse(new NopResponse(contentType, status));

            if (headers != null) {
                for (Object key : headers.keySet()) {
                    if (headers.get(key) != null)
                        Context.getHttpResponse().addHeader(key.toString(), headers.get(key).toString());
                }
            }

            return Context.getHttpResponse().getOutputStream();
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
            Context.setControllerResponse(new NopResponse(contentType, status));

            if (headers != null) {
                for (Object key : headers.keySet()) {
                    if (headers.get(key) != null)
                        Context.getHttpResponse().addHeader(key.toString(), headers.get(key).toString());
                }
            }

            return Context.getHttpResponse().getWriter();
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
        return RequestUtils.isXhr();
    }


    /**
     * Helper method, returns user-agent header of the request.
     *
     * @return user-agent header of the request.
     */
    protected String userAgent(){
        return RequestUtils.userAgent();
    }

    /**
     * Synonym for {@link #isXhr()}.
     */
    protected boolean xhr(){
        return RequestUtils.xhr();
    }

    /**
     * Returns instance of {@link AppContext}.
     *
     * @return instance of {@link AppContext}.
     */
    protected AppContext appContext(){
        return RequestUtils.appContext();
    }

    /**
     * Returns a format part of the URI, or null if URI does not have a format part.
     * A format part is defined as part of URI that is trailing after a last dot, as in:
     *
     * <code>/books.xml</code>, here "xml" is a format.
     *
     * @return format part of the URI, or nul if URI does not have it.
     */
    protected String format(){
        return RequestUtils.format();
    }


    /**
     * Returns instance of {@link Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link Route}
     */
    protected Route getRoute(){
        return RequestUtils.getRoute();
    }

    /**
     * Will merge a template and return resulting string. This method is used for just merging some text with dynamic values.
     * Once you have the result, you can send it by email, external web service, save it to a database, etc.
     *
     * @param template name of template - same as in regular templates. Example: <code>"/email-templates/welcome"</code>.
     * @param values values to be merged into template.
     * @return merged string
     */
    protected String merge(String template, Map values){
        StringWriter stringWriter = new StringWriter();
        Configuration.getTemplateManager().merge(values, template, stringWriter);
        return stringWriter.toString();
    }

    /**
     * Returns response headers
     *
     * @return map with response headers.
     */
    public Map<String, String> getResponseHeaders(){
        Collection<String> names  = Context.getHttpResponse().getHeaderNames();
        Map<String, String> headers = new HashMap<String, String>();
        for (String name : names) {
            headers.put(name, Context.getHttpResponse().getHeader(name));
        }
        return headers;
    }
}
