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
package org.javalite.activeweb;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Igor Polevoy
 */
class Context {

    private static ThreadLocal<ControllerRegistry> registry = new ThreadLocal<ControllerRegistry>();
    private static ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();
    private static ThreadLocal<HttpServletResponse> response = new ThreadLocal<HttpServletResponse>();
    private static ThreadLocal<FilterConfig> filterConfig = new ThreadLocal<FilterConfig>();
    private static ThreadLocal<ControllerResponse> controllerResponse = new ThreadLocal<ControllerResponse>();
    private static ThreadLocal<AppContext> appContext = new ThreadLocal<AppContext>();
    private static ThreadLocal<RequestContext> requestContext = new ThreadLocal<RequestContext>();
    private static ThreadLocal<String> format = new ThreadLocal<String>();
    private static ThreadLocal<String> encoding = new ThreadLocal<String>();
    private static ThreadLocal<Route> route = new ThreadLocal<Route>();
    private static ThreadLocal<Map<String, Object>> values =new ThreadLocal<Map<String, Object>>();


    public static Map<String, Object> getValues() {
        return values.get();
    }

    public static String getEncoding() {
        return encoding.get();
    }

    public static void setEncoding(String encoding) {
        Context.encoding.set(encoding);
    }

    public static String getFormat() {
        return format.get();
    }

    public static void setFormat(String format) {
        Context.format.set(format);
    }

    public static RequestContext getRequestContext() {
        return requestContext.get();
    }

    public static void setRequestContext(RequestContext requestContext) {
        Context.requestContext.set(requestContext);
    }

    public static AppContext getAppContext() {
        return appContext.get();
    }

    public static void setAppContext(AppContext appContext) {
        Context.appContext.set(appContext);
    }

    static void setControllerRegistry(ControllerRegistry controllerRegistry){
        registry.set(controllerRegistry);
    }

    static ControllerRegistry getControllerRegistry(){
        return registry.get();
    }

    static void setHttpRequest(HttpServletRequest req){
        request.set(req);
    }

    static HttpServletRequest getHttpRequest(){
        return request.get();
    }

    static void setHttpResponse(HttpServletResponse resp){        
        response.set(resp);
    }

    static HttpServletResponse getHttpResponse(){
        return response.get();
    }

    static ControllerResponse getControllerResponse() {
        return controllerResponse.get();
    }

    static void setControllerResponse(ControllerResponse resp) {
        controllerResponse.set(resp);
    }

    static Route getRoute(){
        return route.get();
    }


    static FilterConfig getFilterConfig() {
        return filterConfig.get();
    }

    static void setFilterConfig(FilterConfig config) {
        filterConfig.set(config);
    }

    static void setTLs(HttpServletRequest req, HttpServletResponse resp, FilterConfig conf,
                       ControllerRegistry reg, AppContext context, RequestContext requestContext, String format) {
        setHttpRequest(req);
        setHttpResponse(resp);
        setControllerRegistry(reg);
        setFilterConfig(conf);
        setAppContext(context);
        setRequestContext(requestContext);
        setFormat(format);
    }

    static void setRoute(Route route) throws InstantiationException, IllegalAccessException {
        if (route == null)
            throw new IllegalArgumentException("Route could not be null");
        if (route.getId() != null){
            getHttpRequest().setAttribute("id", route.getId());
        }
        Context.route.set(route);
        Context.values.set(new HashMap<String, Object>());

    }

    static void clear() {
        registry.set(null);
        request.set(null);
        response.set(null);
        controllerResponse.set(null);
        route.set(null);
        filterConfig.set(null);
        requestContext.set(null);
        format.set(null);
        encoding.set(null);
        appContext.set(null);
        values.set(null);
    }
}
