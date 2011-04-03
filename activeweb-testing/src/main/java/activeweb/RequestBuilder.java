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

import com.google.inject.Injector;
import org.springframework.mock.web.*;


import javax.servlet.http.*;

import java.util.*;

import static activeweb.ControllerFactory.createControllerInstance;
import static activeweb.ControllerFactory.getControllerClassName;

/**
 * Class is used in DSL for building a fake request for a controller to be tested. This class is not used directly.
 * 
 * @author Igor Polevoy
 */
public class RequestBuilder {
    private static final String MULTIPART = "multipart/form-data";

    private boolean integrateViews = false;
    private Map<String, String> values = new HashMap<String, String>();
    private String contentType;
    private byte[] content;
    private String controllerPath;
    private HttpSession session;
    private List<Cookie> cookies = new ArrayList<Cookie>();
    private MockHttpServletRequest request;
    private String realAction;
    private List<FormItem> formItems =  new ArrayList<FormItem>();

    public RequestBuilder(String controllerPath, HttpSession session) {
        this.controllerPath = controllerPath;
        this.session = session;

    }

    /**
     * Adds an "uploaded" file to the request. Do not forget to set the content type to: "multipart/form-data", or
     * this method will be ignored.
     *
     * @param name name of file.
     * @param fieldName name of field name - this is typically a name of a HTML form field.
     * @param isFile set tru for file, false for regular field. 
     * @param contentType this is content type for this field, not the request. Set to a value reflecting the file
     * content, such as "image/png", "applicaiton/pdf", etc. 
     * @param content this is the binary content of the file.
     * @return {@link activeweb.RequestBuilder} for setting additional request parameters.
     */
    public RequestBuilder formItem(String name, String fieldName, boolean isFile, String contentType, byte[] content){
        checkContentType();
        formItems.add(new FormItem(name, fieldName, isFile, contentType, content));
        return this;
    }

    /**
     * Adds "uploaded" file to the request. Do not forget to set the content type to: "multipart/form-data", or
     * this method will be ignored.
     *
     * @param item this can be an instance of a {@link activeweb.FormItem} or {@link activeweb.FileItem}.
     * @return {@link activeweb.RequestBuilder} for setting additional request parameters.
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

    public RequestBuilder param(String name, String value) {        
        values.put(name, value);
        checkParamAndMultipart();
        return this;
    }

    private void checkParamAndMultipart() {
        if(contentType != null && contentType.equals(MULTIPART) && values.size() > 0){
            throw new IllegalArgumentException("cannot use param() with content type: " + MULTIPART + ", use formItem()");
        }
    }

    public RequestBuilder param(String name, Object value) {
        param(name, value.toString());
        return this;
    }

    public RequestBuilder params(String ... namesAndValues){

        if(namesAndValues.length % 2 != 0)
            throw new IllegalArgumentException("number of arguments must be even");


        for (int i = 0; i < namesAndValues.length - 1; i += 2) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("attribute names cannot be nulls");
            param(namesAndValues[i], namesAndValues[i + 1]);
        }
        return this;
    }

    public RequestBuilder contentType(String contentType) {                
        this.contentType = contentType;
        checkParamAndMultipart();
        return this;
    }

    /**
     * Adds cookie to current request.
     */
    public RequestBuilder  cookie(Cookie cookie){
        cookies.add(cookie);
        return this;
    }

    public RequestBuilder content(byte[] content) {
        this.content = content;
        return this;
    }

    public RequestBuilder integrateViews() {
        integrateViews = true;
        return this;
    }

    public RequestBuilder integrateViews(boolean integrateViews) {
        this.integrateViews = integrateViews;
        return this;
    }

    public void get(String actionName) {
        realAction = actionName;
        submitRequest(actionName, HttpMethod.GET);
    }

    public void post(String actionName) {
        realAction = actionName;
        submitRequest(actionName, HttpMethod.POST);
    }

    public void put(String actionName) {
        realAction = actionName;
        submitRequest(actionName, HttpMethod.PUT);
    }

    public void delete(String actionName) {
        realAction = actionName;
        submitRequest(actionName, HttpMethod.DELETE);
    }


    private void submitRequest(String actionName, HttpMethod method) {
        //TODO: refactor this method, getting out of control        
        if(contentType != null && contentType.equals(MULTIPART) && formItems.size() > 0){
            request = new MockMultipartHttpServletRequestImpl();
            for (FormItem item : formItems) {
                ((AWMockMultipartHttpServletRequest) request).addFormItem(item);
            }
        }else{
            request = new MockHttpServletRequest();
        }

        request.setContextPath("/test_context");
        ContextAccess.setHttpRequest(request);


        if(session != null) request.setSession(session);

        if (contentType != null) request.setContentType(contentType);

        if (content != null) request.setContent(content);

        String path = controllerPath + (realAction != null? "/" + realAction: "");
        request.setServletPath(path);
        request.setRequestURI(path);

        addCookiesInternal(request);

        //this is to fake the PUT and DELETE methods, just like a browser
        if(!(method.equals(HttpMethod.GET) || method.equals(HttpMethod.POST))){
            request.setParameter("_method", method.toString());
            request.setMethod("POST");
        }else{
            request.setMethod(method.toString());
        }

        addParameterValues(request);
        try{
            AppController controller = createControllerInstance(getControllerClassName(controllerPath));
            Injector injector = ContextAccess.getControllerRegistry().getInjector();
            if(injector != null && controller.injectable()){
                injector.injectMembers(controller);
            }

            ControllerRunner runner = new ControllerRunner();

            //must reset these two because in tests, we can execute multiple controllers in the same test method.
            ContextAccess.setControllerResponse(null);
            ContextAccess.setHttpResponse(new MockHttpServletResponse());
            ContextAccess.setActionName(actionName);

            runner.run(new MatchedRoute(controller, actionName), true, integrateViews);
        }catch(WebException e){
            throw e;
        }catch(Exception e){
            throw new SpecException(e);
        }
    }

    private void addCookiesInternal(MockHttpServletRequest request) {
        List<javax.servlet.http.Cookie> servletCookieList = new ArrayList<javax.servlet.http.Cookie>();
        for(Cookie cookie: cookies){
            servletCookieList.add(Cookie.toServletCookie(cookie));
        }
        javax.servlet.http.Cookie[] arr =  servletCookieList.toArray(new javax.servlet.http.Cookie[0]);
        request.setCookies(arr);
    }

    private void addParameterValues(MockHttpServletRequest httpServletRequest) {
        String[] keys = values.keySet().toArray(new String[]{});
        for (String key : keys) {
            httpServletRequest.addParameter(key, values.get(key));
        }
    }
}
