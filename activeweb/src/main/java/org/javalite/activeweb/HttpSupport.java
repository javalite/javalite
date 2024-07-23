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


import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.javalite.common.Convert;
import org.javalite.json.JSONHelper;
import org.javalite.common.Util;
import org.javalite.json.JSONList;
import org.javalite.json.JSONMap;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
public class HttpSupport implements RequestAccess {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static Pattern hashPattern = Pattern.compile("\\[.*\\]");

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
        RequestContext.getValues().put(name, value);
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
     * Returns status code from current response.
     *
     * @return status code  or -1 if controller response was not generated (error)
     */
    protected int status() {
        ControllerResponse response = RequestContext.getControllerResponse();
        return response != null ? response.getStatus() : -1;
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
        checkFlasher();
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
        checkFlasher();
        ((Map) session().get("flasher")).put(name, value);
    }

    private void checkFlasher(){
        if (session().get("flasher") == null) {
            session().put("flasher", new HashMap());
        }
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
            RequestContext.getHttpResponse().setContentType(contentType);
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
            RequestContext.getHttpResponse().setHeader(name, value);
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

        /**
         * Alias to {@link #status(int)}.
         *
         * @param status response code.
         */
        public void statusCode(int status){
            status(status);
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
            ControllerResponse response = RequestContext.getControllerResponse();
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
        RenderTemplateResponse resp = new RenderTemplateResponse(values, template, RequestContext.getFormat());
        RequestContext.setControllerResponse(resp);
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
        RequestContext.setControllerResponse(resp);
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
        RequestContext.setControllerResponse(resp);
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
        String referrer = RequestContext.getHttpRequest().getHeader("Referer");
        referrer = referrer == null? defaultReference: referrer;
        RedirectResponse resp = new RedirectResponse(referrer);
        RequestContext.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }


    /**
     * Redirects to referrer if one exists. If a referrer does not exist, it will be redirected to
     * the root of the application.
     *
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder redirectToReferrer() {
        String referrer = RequestContext.getHttpRequest().getHeader("Referer");
        referrer = referrer == null? RequestContext.getHttpRequest().getContextPath(): referrer;
        RedirectResponse resp = new RedirectResponse(referrer);
        RequestContext.setControllerResponse(resp);
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
        String contextPath = RequestContext.getHttpRequest().getContextPath();
        String action = params.get("action") != null? params.get("action").toString() : null;
        String id = params.get("id") != null? params.get("id").toString() : null;
        boolean restful= AppController.restful(controllerClass);
        params.remove("action");
        params.remove("id");

        String uri = contextPath + Router.generate(controllerPath, action, id, restful, params);

        RedirectResponse resp = new RedirectResponse(uri);
        RequestContext.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }

    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to support AJAX or API functions.
     *
     * @param text text of response.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected HttpBuilder respond(String text){
        if(text == null){
            text = "null";
        }
        DirectResponse resp = new DirectResponse(text);
        String contentType = RequestContext.getHttpResponse().getContentType();
        if(contentType != null){
            RequestContext.getHttpResponse().setContentType(contentType);
        }

        RequestContext.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }

    /**
     * Serializes the argument as a JSON string and sets the "Content-Type"  header to "application/json".
     * See {@link HttpSupport#respond(String)}.
     *
     * @param object object to serialize to JSON
     * @return self
     */
    protected HttpBuilder respondJSON(Object object){
        header("Content-Type", "application/json");
        return respond(JSONHelper.toJSON(object));
    }

    /**
     * Convenience method for downloading files. This method will force the browser to find a handler(external program)
     *  for  this file (content type) and will provide a name of file to the browser. This method sets an HTTP header
     * "Content-Disposition" based on a file name.
     *
     * @param file file to download.
     * @param delete true to delete the file after processing
     * @return builder instance.
     */
    protected HttpBuilder sendFile(File file, boolean delete)  {
        try{
            FileResponse resp = new FileResponse(file, delete);
            RequestContext.setControllerResponse(resp);
            HttpBuilder builder = new HttpBuilder(resp);
            builder.header("Content-Disposition", "attachment; filename=" + file.getName());
            return builder;
        }catch(Exception e){
            throw new ControllerException(e);
        }
    }

    /**
     * Convenience method for downloading files. This method will force the browser to find a handler(external program)
     *  for  this file (content type) and will provide a name of file to the browser. This method sets an HTTP header
     * "Content-Disposition" based on a file name.
     *
     * @param file file to download.
     * @return builder instance.
     */
    protected HttpBuilder sendFile(File file) {
       return sendFile(file, false);
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
     * Direct access to current <code>HttpServletRequest</code> for low level operations.
     *
     * @return instance of current <code>HttpServletRequest</code>.
     */
    protected HttpServletRequest getHttpServletRequest(){
        return RequestContext.getHttpRequest();
    }
    
    /**
     * Direct access to current <code>HttpServletResponse</code> for low level operations.
     *
     * @return instance of current <code>HttpServletResponse</code>.
     */
    protected HttpServletResponse getHttpServletResponse(){
        return RequestContext.getHttpResponse();
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
        List<FormItem> fileItems = new ArrayList<>();
        for(FormItem item : multipartFormItems(encoding, maxFileSize)) {
            if (item.isFile()) {
                fileItems.add(item);
            }
        }
        return fileItems.iterator();
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
     * The size of upload defaults to max of 20mb. Files greater than that will be rejected. If you want to accept larger files, create a file called <code>activeweb.properties</code>,
     * add it to your classpath and place this property to the file:
     *
     * <pre>
     * #max upload size
     * maxUploadSize = 20000000
     * </pre>
     *
     * Alternatively, just call this method and pass a per-request parameter for the size: {@link #multipartFormItems(String, long)}.
     *
     *
     * @param encoding specifies the character encoding to be used when reading the headers of individual part.
     * When not specified, or null, the request encoding is used. If that is also not specified, or null,
     * the platform default encoding is used.
     *
     * @return a collection of uploaded files from a multi-part request.
     */
    protected List<FormItem> multipartFormItems(String encoding) {
        return multipartFormItems(encoding, Configuration.getMaxUploadSize());
    }


    /**
     * @return {@link MultipartForm} object for convenience.
     */
    protected MultipartForm multipartForm(){
        return multipartForm("UTF-8", Configuration.getMaxUploadSize());
    }

    /**
     * @param encoding encoding to use to read values from request
     * @param maxUploadSize set max upload size
     *
     * @return {@link MultipartForm} object for convenience.
     */
    protected MultipartForm multipartForm(String encoding, long maxUploadSize){
        MultipartForm multipartForm = new MultipartForm();
        List<FormItem> formItems = multipartFormItems(encoding, maxUploadSize);
        for (FormItem formItem : formItems) {
            if(formItem instanceof org.javalite.activeweb.FileItem){
                multipartForm.addFileItem((org.javalite.activeweb.FileItem) formItem);
            }else {
                multipartForm.addFormItem(formItem);
            }
        }
        return multipartForm;
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
     * @param encoding specifies the character encoding to be used when reading the headers of individual part.
     * When not specified, or null, the request encoding is used. If that is also not specified, or null,
     * the platform default encoding is used.
     *
     * @param maxUploadSize maximum size of the upload in bytes. A value of -1 indicates no maximum.
     *
     * @return a collection of uploaded files from a multi-part request.
     */
    protected List<FormItem> multipartFormItems(String encoding, long maxUploadSize) {
        //we are thread safe, because controllers are pinned to a thread and discarded after each request.
        if(RequestContext.getFormItems() != null ){
            return RequestContext.getFormItems();
        }

        HttpServletRequest req = RequestContext.getHttpRequest();

        if (req instanceof AWMockMultipartHttpServletRequest) {//running inside a test, and simulating upload.
            RequestContext.setFormItems(((AWMockMultipartHttpServletRequest) req).getFormItems());
        } else {

            if (!ServletFileUpload.isMultipartContent(req))
                throw new ControllerException("this is not a multipart request, be sure to add this attribute to the form: ... enctype=\"multipart/form-data\" ...");

            DiskFileItemFactory factory = new DiskFileItemFactory();

            factory.setSizeThreshold(Configuration.getMaxUploadSize());
            factory.setRepository(Configuration.getTmpDir());

            ServletFileUpload upload = new ServletFileUpload(factory);
            if(encoding != null)
                upload.setHeaderEncoding(encoding);
            upload.setFileSizeMax(maxUploadSize);
            try {
                List<org.apache.commons.fileupload.FileItem> apacheFileItems = upload.parseRequest(RequestContext.getHttpRequest());
                ArrayList items = new ArrayList<>();
                for (FileItem apacheItem : apacheFileItems) {
                    ApacheFileItemFacade f = new ApacheFileItemFacade(apacheItem);
                    if(f.isFormField()){
                        items.add(new FormItem(f));
                    }else{
                        items.add(new org.javalite.activeweb.FileItem(f));
                    }
                }
                RequestContext.setFormItems(items);
            } catch (Exception e) {
                throw new ControllerException(e);
            }
        }
        return RequestContext.getFormItems();
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
    protected Map<String, String> getMap(String hashName) {
        Map<String, String[]>  params = params();
        Map<String, String>  hash = new HashMap<>();
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
    protected Map<String, String> getMap(String hashName, List<FormItem> formItems) {
        Map<String, String>  hash = new HashMap<>();
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


    /**
     * Sets character encoding for request. Has to be called before reading any parameters of getting input
     * stream.
     * @param encoding encoding to be set.
     */
    protected void setRequestEncoding(String encoding) {

        try{
            RequestContext.getHttpRequest().setCharacterEncoding(encoding);
        }catch(Exception e){
            throw new WebException(e);
        }
    }


    /**
     * Sets character encoding for response.
     *
     * @param encoding encoding to be set.
     */
    protected void setResponseEncoding(String encoding) {
        RequestContext.getHttpResponse().setCharacterEncoding(encoding);
    }



    /**
     * Sets character encoding on the response.
     *
     * @param encoding character encoding for response.
     */
    protected void setEncoding(String encoding){
        RequestContext.setEncoding(encoding);
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
        RequestContext.getHttpResponse().setContentLength(length);
    }
    /**
     * Sets locale on response.
     *
     * @param locale locale for response.
     */
    protected void setLocale(Locale locale){
        RequestContext.getHttpResponse().setLocale(locale);
    }

    /**
     * Same as {@link #setLocale(java.util.Locale)}
     *
     * @param locale locale for response
     */
    protected void locale(Locale locale){
        RequestContext.getHttpResponse().setLocale(locale);
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
     * Sends cookie to browse with response.
     *
     * @param cookie cookie to send.
     */
    protected void sendCookie(Cookie cookie){
        RequestContext.getHttpResponse().addCookie(Cookie.toServletCookie(cookie));
    }

    /**
     * Sends cookie to browse with response.
     *
     * @param name name of cookie
     * @param value value of cookie.
     */
    protected void sendCookie(String name, String value) {
        RequestContext.getHttpResponse().addCookie(Cookie.toServletCookie(new Cookie(name, value)));
    }


    /**
     * Sends long to live cookie to browse with response. This cookie will be asked to live for 20 years.
     *
     * @param name name of cookie
     * @param value value of cookie.
     */
    protected void sendPermanentCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(60*60*24*365*20);
        RequestContext.getHttpResponse().addCookie(Cookie.toServletCookie(cookie));
    }


    /**
     * Adds a header to response.
     *
     * @param name name of header.
     * @param value value of header.
     */
    protected void header(String name, String value){
        RequestContext.getHttpResponse().addHeader(name, value);
    }

    /**
     * Adds multiple header values to response. A single header can have multiple values.
     *
     * @param name name of header.
     * @param values multiple values for the same header.
     */
    protected void header(String name, String ... values){
        for (String value : values) {
            RequestContext.getHttpResponse().addHeader(name, value);
        }
    }

    /**
     * A convenience method. Sets the <code>"Content-Type"</code> header on the response to
     * <code>"application/json"</code>.
     */
    protected void applicationJSON(){
        RequestContext.getHttpResponse().addHeader("Content-Type", "application/json");
        RequestContext.getHttpResponse().setContentType("application/json");
    }

    /**
     * A convenience method. Sets the <code>"Content-Type"</code> header on the response.
     *
     * @param contentType value of a Content-type header.
     */
    protected void contentType(String contentType){
        RequestContext.getHttpResponse().addHeader("Content-Type", contentType);
        RequestContext.getHttpResponse().setContentType(contentType);
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
        RequestContext.setControllerResponse(resp);
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
        return RequestContext.getFilterConfig().getServletContext().getRealPath(path);
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
            addHeaders(contentType, headers, status);
            return RequestContext.getHttpResponse().getOutputStream();
        }catch(Exception e){
            throw new ControllerException(e);
        }
    }


    private void addHeaders(String contentType, Map headers, int status){

        RequestContext.setControllerResponse(new NopResponse(status));

        if (headers != null) {
            for (Object key : headers.keySet()) {
                if (headers.get(key) != null){
                    RequestContext.getHttpResponse().addHeader(key.toString(), headers.get(key).toString());
                    if(key.toString().equalsIgnoreCase("content-type")){
                        RequestContext.getHttpResponse().setContentType(headers.get(key).toString());
                    }
                }
            }
        }

        //override the content-type by the method argument such as outputStream("application/json", ...);
        if(contentType != null){
            RequestContext.getHttpResponse().setContentType(contentType);
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
            addHeaders(contentType, headers, status);
            return RequestContext.getHttpResponse().getWriter();
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
        for(String name:names){
            if(Util.blank(param(name))){
                return true;
            }
        }
        return false;
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
        Configuration.getTemplateManager().merge(values, template, stringWriter, RequestContext.getRoute().isCustom());
        return stringWriter.toString();
    }

    /**
     * Returns response headers
     *
     * @return map with response headers.
     */
    protected Map<String, String> getResponseHeaders(){
        Collection<String> names  = RequestContext.getHttpResponse().getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        for (String name : names) {
            headers.put(name, RequestContext.getHttpResponse().getHeader(name));
        }
        return headers;
    }

    /**
     * Cleans HTML from harmful tags, making XSS impossible.
     * <p>For example, input like this:</p>
     *
     * <pre>
     *      &lt;html&gt;&lt;script&gt; alert('hello');&lt;/script&gt;&lt;div&gt;this is a clean part&lt;/div&gt;&lt;/html&gt;
     * </pre>
     *
     * Will produce output like this:
     *
     * <pre>
     *     this is a clean part
     * </pre>
     *
     * @param unsafeContent unsafe content. Something that an end user typed into a text area, or input that may include
     *                      a script tag or other garbage.
     * @return sanitized version of input
     */
    protected String sanitize(String unsafeContent){
        return Jsoup.clean(unsafeContent, Safelist.basic());
    }

    /**
     * Returns InputStream of the request.
     *
     * @return InputStream of the request
     */
    protected InputStream getRequestInputStream() {
        try {
            return RequestContext.getHttpRequest().getInputStream();
        } catch (IOException e) {
            throw new WebException(e);
        }
    }

    /**
     * Alias to {@link #getRequestInputStream()}.
     * @return input stream to read data sent by client.
     *
     */
    protected InputStream getRequestStream() {
        try {
            return RequestContext.getHttpRequest().getInputStream();
        } catch (IOException e) {
            throw new WebException(e);
        }
    }


    /**
     * Reads entire request data and converts it to {@link JSONMap}.
     *
     * @return data sent by client as {@link JSONMap}.
     */
    protected JSONMap getRequestJSONMap() {
        return new JSONMap(getRequestString());
    }

    /**
     * Reads entire request data and converts it to {@link JSONList}.
     *
     * @return data sent by client as {@link JSONList}.
     */
    protected JSONList getRequestJSONList() {
        return new JSONList(getRequestString());
    }

    /**
     * Reads entire request data as String. Do not use for large data sets to avoid
     * memory issues, instead use {@link #getRequestInputStream()}.
     *
     * @return data sent by client as string.
     */
    protected String getRequestString() {
        try {
            return Util.read(RequestContext.getHttpRequest().getInputStream());
        } catch (IOException e) {
            throw new WebException(e);
        }
    }

    /**
     * Reads entire request data as byte array. Do not use for large data sets to avoid
     * memory issues.
     *
     * @return data sent by client as string.
     */
    protected byte[] getRequestBytes() {

        try {
            return Util.bytes(RequestContext.getHttpRequest().getInputStream());
        } catch (IOException e) {
            throw new WebException(e);
        }
    }


    /**
     * Converts posted JSON array to a Java List. Example of a JSON array: <code>[1, 2, 3]</code>.
     *
     * @return Java List converted from posted JSON string.
     */
    protected List jsonList() {
        checkJsonContentType();
        return JSONHelper.toList(getRequestString());
    }

    private void checkJsonContentType(){
        if(!(header("Content-Type") != null && header("Content-Type").toLowerCase().contains("application/json")) ){
            throw new WebException("Trying to convert JSON to object, but Content-Type is " + header("Content-Type") + ", not 'application/json'");
        }
    }

    /**
     * Converts posted JSON map to a Java Map. Example JSON map: <code>{"name":"John", "age":21}</code>.
     *
     * @return Java Map converted from posted JSON string map.
     */
    protected Map jsonMap() {
        checkJsonContentType();
        return JSONHelper.toMap(getRequestString());

    }


    /**
     * Converts posted JSON maps to a Java Maps array. Example JSON map: <code>[{"name":"John", "age":21}, {"name":"Jane", "age":20}]</code>.
     *
     * @return Java Maps converted from posted JSON string maps.
     */
    protected Map[] jsonMaps() {
        checkJsonContentType();
        return JSONHelper.toList(getRequestString()).getMaps();
    }


    /**
     * Returns a mutable Map with all the values from the current request context. Use this to get/put/modify values from
     * current place on the stack down stream (from filters to controllers, to views).
     *
     * @return a mutable <code>Map</code> with all values from/for current request context.
     */
    protected Map values() {
        return RequestContext.getValues();
    }
}
