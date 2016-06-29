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

import com.google.inject.Injector;
import org.springframework.mock.web.*;


import java.util.*;

import static org.javalite.activeweb.ControllerFactory.createControllerInstance;
import static org.javalite.activeweb.ControllerFactory.getControllerClassName;
import static org.javalite.test.jspec.JSpec.a;

/**
 * Class is used in DSL for building a fake request for a controller to be tested. This class is not used directly.
 * 
 * @author Igor Polevoy
 */
public class RequestBuilder {
    private static final String MULTIPART = "multipart/form-data";

    private boolean integrateViews;
    private Map<String, Object> values = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String contentType;
    private byte[] content;
    private String controllerPath;
    private SessionTestFacade sessionFacade;
    private List<org.javalite.activeweb.Cookie> cookies = new ArrayList<>();
    private MockHttpServletRequest request;
    private String realAction;
    private List<FormItem> formItems = new ArrayList<>();
    private String id;
    private String queryString;
    private String format;

    public RequestBuilder(String controllerPath, SessionTestFacade sessionFacade, boolean integrateViews) {
        this.controllerPath = controllerPath;
        this.sessionFacade = sessionFacade;
        if(integrateViews)
            integrateViews();
    }


    /**
     * Convenience method to add a non-file form item to request.
     * Will call {@link #formItem(String, String, boolean, String, byte[])} internally.
     * Content type will be set to "text/plain", and "isFile" to false.
     *
     * @param fieldName name of field - toString() will be used to add to form item
     * @param value - value of parameter, toString().getBytes() will be used to add to form item
     * @return @return {@link org.javalite.activeweb.RequestBuilder} for setting additional request parameters.
     *
     * @see {@link #formItem(String, String, boolean, String, byte[])}
     *
     */
    public RequestBuilder formItem(Object fieldName, Object value){
        a(fieldName).shouldNotBeNull();
        a(value).shouldNotBeNull();
        return formItem(fieldName.toString(), fieldName.toString(), false, "text/plain", value.toString().getBytes());
    }

    /**
     * Convenience method for sending pairs of name and values with multi-part request.
     *
     * @param namesAndValues names and following corresponding values. The following pattern is expected:
     *                       name,value,name1,value1...
     * @return @return {@link org.javalite.activeweb.RequestBuilder} for setting additional request parameters.
     */
    public RequestBuilder formItems(Object ... namesAndValues){
        if(namesAndValues.length % 2 != 0)
            throw new IllegalArgumentException("number of arguments must be even");

        for (int i = 0; i < namesAndValues.length - 1; i += 2) {
            a(namesAndValues[i]).shouldNotBeNull();
            a(namesAndValues[i + 1]).shouldNotBeNull();
            formItem(namesAndValues[i].toString(), namesAndValues[i].toString(), false, "text/plain", namesAndValues[i + 1].toString().getBytes());
        }
        return this;
    }

    /**
     * Adds an "uploaded" file to the request. Do not forget to set the content type to: "multipart/form-data", or
     * this method will be ignored.
     *
     * @param fileName name of file.
     * @param fieldName name of field name - this is typically a name of a HTML form field.
     * @param isFile set true for file, false for regular field. 
     * @param contentType this is content type for this field, not the request. Set to a value reflecting the file
     * content, such as "image/png", "application/pdf", etc.
     * @param content this is the binary content of the file.
     * @return {@link org.javalite.activeweb.RequestBuilder} for setting additional request parameters.
     */
    public RequestBuilder formItem(String fileName, String fieldName, boolean isFile, String contentType, byte[] content){
        checkContentType();
        formItems.add(new FormItem(fileName, fieldName, isFile, contentType, content));
        return this;
    }

    /**
     * Adds "uploaded" file to the request. Do not forget to set the content type to: "multipart/form-data", or
     * this method will be ignored.
     *
     * @param item this can be an instance of a {@link org.javalite.activeweb.FormItem} or {@link org.javalite.activeweb.FileItem}.
     * @return {@link org.javalite.activeweb.RequestBuilder} for setting additional request parameters.
     */
    public RequestBuilder formItem(FormItem item){
        checkContentType();
        formItems.add(item);
        return this;
    }

    private void checkContentType(){
        if(contentType == null || !contentType.equals(MULTIPART)){
            throw new IllegalArgumentException("Must set content type to: 'multipart/form-data' before adding a new form item" );
        }
    }

    private void checkParamAndMultipart() {
        if(contentType != null && contentType.equals(MULTIPART) && values.size() > 0){
            throw new IllegalArgumentException("cannot use param() with content type: " + MULTIPART + ", use formItem()");
        }
    }

    /**
     * Sets a single parameter for request.
     * <p>
     *     For parameters with multiple values, set the value to be <code>List<String></code>
     * </p>
     *
     * @param name name of parameter.
     * @param value value of parameter.
     * @return instance of RequestBuilder.
     */
    public RequestBuilder param(String name, Object value) {

        if(name == null || value == null) throw new IllegalArgumentException("neither argument can be null");

        if(value instanceof List){
            List list = (List) value;
            values.put(name, list);
        }else{
            values.put(name, value.toString());
        }
        return this;
    }


    /**
     * Convenience method, exists to pass parameters with blank values.
     * Calling this method such as:  <code>param("flag")</code> is equivalent to:
     * <code>param("flag", "")</code>
     *
     * @param name name of parameter to pass
     * @return instance of RequestBuilder.
     */
    public RequestBuilder param(String name) {

        if(name == null) throw new IllegalArgumentException("name can't be null");

        values.put(name, "");
        return this;
    }

    /**
     * Sets a single header for the request.
     *
     * @param name name of header.
     * @param value value of header.
     * @return instance of RequestBuilder.
     */
    public RequestBuilder header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Convenience method to set names and values for headers. If arguments are indexed
     * from 1, then every odd argument is a name and every even argument that follows it is a value corresponding
     * to the preceding odd argument.
     *
     * @param namesAndValues names and following corresponding values
     * @return instance of RequestBuilder
     */
    public RequestBuilder headers(String ... namesAndValues) {

        if(namesAndValues.length % 2 != 0)
            throw new IllegalArgumentException("number of arguments must be even");

        for (int i = 0; i < namesAndValues.length - 1; i += 2) {
            if (namesAndValues[i] == null || namesAndValues[i + 1] == null)
                throw new IllegalArgumentException("header names or values cannot be null");
            header(namesAndValues[i], namesAndValues[i + 1]);
        }
        return this;
    }

    /**
     * Convenience method for setting parameters of the request. If arguments are indexed
     * from 1, then every odd argument is a name and every even argument that follows it is a value corresponding
     * to the preceding odd argument.
     * <p>
     *     For parameters with multiple values, set the value to be <code>List<String></code>
     * </p>
     *
     * @param namesAndValues names and following corresponding values
     * @return instance of RequestBuilder.
     */
    public RequestBuilder params(Object ... namesAndValues){

        if(namesAndValues.length % 2 != 0)
            throw new IllegalArgumentException("number of arguments must be even");


        for (int i = 0; i < namesAndValues.length - 1; i += 2) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("parameter names cannot be nulls");
                param(namesAndValues[i].toString(), namesAndValues[i + 1]);
        }
        return this;
    }

    /**
     * Sets content type on request.
     *
     * @param contentType content type.
     * @return instance of RequestBuilder
     */
    public RequestBuilder contentType(String contentType) {                
        this.contentType = contentType;
        return this;
    }

    /**
     * Adds cookie to current request.
     */
    public RequestBuilder  cookie(org.javalite.activeweb.Cookie cookie){
        cookies.add(cookie);
        return this;
    }

    public RequestBuilder content(byte[] content) {
        this.content = content;
        return this;
    }

    /**
     * Call this method to cause generation of the view after execution of a controller.
     * If this method is used, the content of generated HTML will be available with <code>responseContent()</code>.
     *
     * @return instance of RequestBuilder
     */
    public RequestBuilder integrateViews() {
        integrateViews(true);
        return this;
    }

    /**
     * Call this method to cause generation of the view after execution of a controller.
     * If this method is used, the content of generated HTML will be available with <code>responseContent()</code>.
     *
     * @param integrateViews true to integrate views, false not to.
     * @return instance of RequestBuilder
     */
    public RequestBuilder integrateViews(boolean integrateViews) {
        this.integrateViews = integrateViews;
        return this;
    }

    /**
     * Simulate HTTP GET call to an action of controller.
     *
     * @param actionName name of action as on a URL - not CamelCase.
     */
    public void get(String actionName) {
        realAction = actionName;
        submitRequest(actionName, HttpMethod.GET);
    }

    /**
     * Simulate HTTP POST call to an action of controller.
     *
     * @param actionName name of action as on a URL - not CamelCase.
     */
    public void post(String actionName) {
        realAction = actionName;
        submitRequest(actionName, HttpMethod.POST);
    }

    /**
     * Simulate HTTP PUT call to an action of controller.
     *
     * @param actionName name of action as on a URL - not CamelCase.
     */
    public void put(String actionName) {
        realAction = actionName;
        submitRequest(actionName, HttpMethod.PUT);
    }

    /**
     * Simulate HTTP DELETE call to an action of controller.
     *
     * @param actionName name of action as on a URL - not CamelCase.
     */
    public void delete(String actionName) {
        realAction = actionName;
        submitRequest(actionName, HttpMethod.DELETE);
    }

    /**
     * Simulate HTTP OPTIONS call to an action of controller.
     *
     * @param actionName name of action as on a URL - not CamelCase.
     */
    public void options(String actionName) {
        realAction = actionName;
        submitRequest(actionName, HttpMethod.OPTIONS);
    }


    private void submitRequest(String actionName, HttpMethod method) {

        checkParamAndMultipart();

        //TODO: refactor this method, getting out of control        
        if(contentType != null && contentType.equals(MULTIPART) && !formItems.isEmpty()){
            request = new MockMultipartHttpServletRequestImpl();
            for (FormItem item : formItems) {
                ((AWMockMultipartHttpServletRequest) request).addFormItem(item);
            }
        }else{
            request = new MockHttpServletRequest();
        }

        request.setContextPath("/test_context");
        Context.setHttpRequest(request);
        Context.setFormat(format);


        if(sessionFacade != null)
            request.setSession(sessionFacade.getSession());

        if (contentType != null)
            request.setContentType(contentType);

        if (content != null)
            request.setContent(content);

        String path = controllerPath + (realAction != null? "/" + realAction: "");
        if(!path.startsWith("/")){
            path = "/" + path;
        }
        request.setServletPath(path);
        request.setRequestURI(path);
        request.setAttribute("id", id);
        request.setQueryString(queryString);

        addCookiesInternal(request);

        //this is to fake the PUT and DELETE methods, just like a browser
        if(method.equals(HttpMethod.PUT)){
            request.setParameter("_method", method.toString());
            request.setMethod("POST");
        }else{
            request.setMethod(method.toString());
        }
        Context.getRequestContext().set("integrateViews", integrateViews);
        addHeaders(request);
        addParameterValues(request);
        try{
            AppController controller = createControllerInstance(getControllerClassName(controllerPath));
            Context.setRoute(new Route(controller, realAction, id));
            Injector injector = Context.getControllerRegistry().getInjector();

            if(injector != null){
                 injector.injectMembers(controller);
            }
            ControllerRunner runner = new ControllerRunner();

            //must reset these two because in tests, we can execute multiple controllers in the same test method.
            Context.setControllerResponse(null);
            Context.setHttpResponse(new MockHttpServletResponse());

            runner.run(new Route(controller, actionName),  integrateViews);
        }catch(WebException e){
            throw e;
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new SpecException(e);
        }
    }

    private void addHeaders(MockHttpServletRequest request) {        
        for(String header: headers.keySet()){
            request.addHeader(header, headers.get(header));
        }
    }

    private void addCookiesInternal(MockHttpServletRequest request) {
        List<javax.servlet.http.Cookie> servletCookieList = new ArrayList<>();
        for(org.javalite.activeweb.Cookie cookie: cookies){
            servletCookieList.add(org.javalite.activeweb.Cookie.toServletCookie(cookie));
        }
        javax.servlet.http.Cookie[] arr =  servletCookieList.toArray(new javax.servlet.http.Cookie[0]);
        request.setCookies(arr);
    }

    private void addParameterValues(MockHttpServletRequest httpServletRequest) {
        for (String key : values.keySet()) {
            Object value = values.get(key);
            if(value instanceof List){
                List<String> strings = new ArrayList<>(((List) value).size());
                for (Object v: ((List)value)) {
                    strings.add(v.toString());
                }
                httpServletRequest.addParameter(key, strings.toArray(new String[]{}));
            }else{
                httpServletRequest.addParameter(key, value.toString());
            }
        }
    }


    /**
     * Sets ID for this request. This method will convert ID value to string before executing a controller.
     *
     * @param id id for this request;  this value is accessible inside controller with <code>getId()</code> method.
     */
    public RequestBuilder id(Object id) {
        if(id == null) throw new IllegalArgumentException("id can't be null");
        this.id = id.toString();
        return this;
    }

    /**
     * Sets format for this request. Format is the part of URI that is trailing after a last dot, as in: /books.xml,
     * here "xml" is a format.
     *
     * @see {@link HttpSupport#format()}
     *
     * @param format format for this request.
     */
    public RequestBuilder format(String format) {
        if(format == null) throw new IllegalArgumentException("format can't be null");
        this.format = format;
        return this;
    }


    /**
     * Sets a query string (as in URL) for the request.
     *
     * @param queryString query string value
     * @return instance of RequestBuilder.
     */
    public RequestBuilder queryString(String queryString) {
        this.queryString = queryString;
        return this;
    }
}
