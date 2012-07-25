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


/**
 * @author Igor Polevoy
 */
class Context {

    private static ThreadLocal<ControllerRegistry> registry = new ThreadLocal<ControllerRegistry>();
    private static ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();
    private static ThreadLocal<HttpServletResponse> response = new ThreadLocal<HttpServletResponse>();
    private static ThreadLocal<FilterConfig> filterConfig = new ThreadLocal<FilterConfig>();
    private static ThreadLocal<ControllerResponse> controllerResponse = new ThreadLocal<ControllerResponse>();
    private static ThreadLocal<String> actionName = new ThreadLocal<String>();
    private static ThreadLocal<String> controllerPath = new ThreadLocal<String>();
    private static ThreadLocal<Boolean> restful = new ThreadLocal<Boolean>();
    private static ThreadLocal<AppContext> appContext = new ThreadLocal<AppContext>();
    private static ThreadLocal<RequestContext> requestContext = new ThreadLocal<RequestContext>();
    private static ThreadLocal<String> format = new ThreadLocal<String>();
    private static ThreadLocal<String> encoding = new ThreadLocal<String>();


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

    static String getActionName(){
        return actionName.get();
    }

    static void setActionName(String name){
        actionName.set(name);
    }

    static String getControllerPath(){
        return controllerPath.get();
    }

    static void setControllerPath(String cPath){
        controllerPath.set(cPath);
    }

    static FilterConfig getFilterConfig() {
        return filterConfig.get();
    }

    static void setFilterConfig(FilterConfig config) {
        filterConfig.set(config);
    }

    static Boolean isRestful() {
        return restful.get();
    }

    static void setRestful(Boolean restfulVal) {
        restful.set(restfulVal);
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
        if(route != null && route.getId() != null){
            getHttpRequest().setAttribute("id", route.getId());
        }
        setControllerPath(Router.getControllerPath(route.getController().getClass()));
        setActionName(route.getActionName());
        setRestful(route.getController().restful());
    }

    static void clear() {
        registry.set(null);
        request.set(null);
        response.set(null);
        controllerResponse.set(null);
        actionName.set(null);
        controllerPath.set(null);
        filterConfig.set(null);
        requestContext.set(null);
        format.set(null);
        encoding.set(null);
    }
}
