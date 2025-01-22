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

import com.google.inject.Injector;
import org.javalite.json.JSONHelper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.javalite.activeweb.ControllerFactory.createControllerInstance;
import static org.javalite.activeweb.ControllerFactory.getControllerClassName;
import static org.javalite.common.Collections.list;
import static org.javalite.test.jspec.JSpec.a;

/**
 * Class is used in DSL for building a fake request for a controller to be tested. This class is not used directly.
 * 
 * @author Igor Polevoy
 */
public class RequestBuilder {
    private static final String MULTIPART = "multipart/form-data";

    private Map<String, List> values = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String contentType;
    private byte[] content;
    private String controllerPath;
    private List<org.javalite.activeweb.Cookie> cookies = new ArrayList<>();
    private MockHttpServletRequest request;
    private String realAction;
    private List<FormItem> formItems = new ArrayList<>();
    private String id;
    private String queryString;
    private String format;
    private String remoteAddress;

    public RequestBuilder(String controllerPath) {
        RequestContext.setControllerResponse(null);
        RequestContext.setParams1st(null);
        RequestContext.setControllerResponse(null);
        RequestContext.setEncoding(null);
        RequestContext.setFormat(null);
        RequestContext.setFormItems(null);
        RequestContext.setHttpResponse(null);
        this.controllerPath = controllerPath;
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
    public RequestBuilder formItem(String fieldName, Object value){
        a(fieldName).shouldNotBeNull();
        a(value).shouldNotBeNull();
        return formItem(null, fieldName, false, "text/plain", value.toString().getBytes());
    }

    /**
     * Convenience method for sending pairs of name and values with multipart request.
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
        if(contentType == null || !contentType.startsWith(MULTIPART)){
            throw new IllegalArgumentException("Must set content type to: 'multipart/form-data' before adding a new form item" );
        }
    }

    private void checkParamAndMultipart() {
        if(contentType != null && contentType.startsWith(MULTIPART) && values.size() > 0){
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
            values.put(name, list(value));
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

        values.put(name, list(""));
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


    /**
     * Use  to post content to a tested controller.
     *
     * @param content content string
     * @return self
     */
    public RequestBuilder content(String content) {
        return content(content.getBytes());
    }


    /**
     * Convenience method. Expects the content to be some JSON document (object or array).
     * This method does NOT check the validity of the document. It is a responsibility of the controller.
     * <br>
     * This method will automatically set the header "Content-Type" to be "application/json".
     *
     * @param content JSON document is expected
     * @return self.
     */
    public RequestBuilder json(String content) {
        contentType("application/json");
        return content(content.getBytes());
    }

    /**
     * Convenience method. Will serialize the  object to JSON.
     * s
     * <br>
     * This method will automatically set the header "Content-Type"
     * to be "application/json".
     *
     * @param request any object that can be serialized to JSON.
     * @return self.
     */
    public RequestBuilder json(Object request) {
        contentType("application/json");
        return content(JSONHelper.toJSON(request));
    }

    /**
     * Use  to post content to a tested controller.
     *
     * @param content content bytes
     * @return self
     */
    public RequestBuilder content(byte[] content) {
        this.content = content;
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


    private void submitRequest(String actionName, HttpMethod method) throws RuntimeException {

        checkParamAndMultipart();
        createAndConfigureRequest(method);

        try{
            AppController controller = createControllerInstance(getControllerClassName(controllerPath));
            RequestContext.setRoute(new Route(controller, realAction, id, method));
            Injector injector = Configuration.getInjector();

            if(injector != null){
                 injector.injectMembers(controller);
            }
            ControllerRunner runner = new ControllerRunner();

            //must reset these two because in tests, we can execute multiple controllers in the same test method.
            RequestContext.setControllerResponse(null);
            RequestContext.setHttpResponse(new MockHttpServletResponse());

            runner.run(new Route(controller, actionName, method));
        } catch(RuntimeException e){
            throw e;
        } catch(Exception e){
            throw new SpecException(e);
        }
    }

    private void createAndConfigureRequest(HttpMethod method) {

        HttpServletRequest prevRequest = RequestContext.getHttpRequest();

        if(contentType != null && contentType.startsWith(MULTIPART) && !formItems.isEmpty()){
            request = new MockMultipartHttpServletRequestImpl();
            for (FormItem item : formItems) {
                ((AWMockMultipartHttpServletRequest) request).addFormItem(item);
            }
        }else{
            request = new MockHttpServletRequest();
        }

        request.setContextPath("/test_context");
        RequestContext.setHttpRequest(request);
        RequestContext.setFormat(format);


        if(remoteAddress != null){
            request.setRemoteAddr(remoteAddress);
        }

        if (prevRequest != null) {
            request.setSession(prevRequest.getSession(false));
        }

        if (contentType != null)
            request.setContentType(contentType);

        if (content != null)
            request.setContent(content);

        String path = controllerPath + (realAction != null? "/" + realAction: "");
        if(!path.startsWith("/")){
            path = "/" + path;
        }
        request.setRequestURI(path);
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
        addHeaders(request);
        addParameterValues(values, request);
        addParameterValuesFromQueryString(request);
    }


    private void addHeaders(MockHttpServletRequest request) {        
        for(String header: headers.keySet()){
            request.addHeader(header, headers.get(header));
        }
    }

    private void addCookiesInternal(MockHttpServletRequest request) {
        List<jakarta.servlet.http.Cookie> servletCookieList = new ArrayList<>();
        for(org.javalite.activeweb.Cookie cookie: cookies){
            servletCookieList.add(org.javalite.activeweb.Cookie.toServletCookie(cookie));
        }
        jakarta.servlet.http.Cookie[] arr =  servletCookieList.toArray(new jakarta.servlet.http.Cookie[0]);
        request.setCookies(arr);
    }

    private void addParameterValues(Map<String, List> valuesMap, MockHttpServletRequest httpServletRequest) {
        for (String key : valuesMap.keySet()) {
            Object value = valuesMap.get(key);
            List<String> strings = new ArrayList<>(((List) value).size());
            for (Object v: ((List)value)) {
                strings.add(v == null? "" : v.toString());
            }
            httpServletRequest.addParameter(key, strings.toArray(new String[]{}));
        }
    }


    private void addParameterValuesFromQueryString(MockHttpServletRequest request) {
        String queryString = request.getQueryString();
        if(queryString != null){
            Map<String, List> params = splitQuery(queryString);
            addParameterValues(params, request);
        }
    }

    // below is borrowed from: https://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection
    private Map<String, List> splitQuery(String  uri) {
            final Map<String, List> query_pairs = new LinkedHashMap<>();
            final String[] pairs = uri.split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<String>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
                query_pairs.get(key).add(value);
            }
            return query_pairs;

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

    /**
     * Use to simulate a remote IP address. Use {@link AppController#remoteAddress()} to retrieve it.
     *
     * @param remoteAddress  simulated remote IP address.
     *
     */
    public RequestBuilder remoteAddress(String remoteAddress){
        this.remoteAddress = remoteAddress;
        return this;
    }

}
