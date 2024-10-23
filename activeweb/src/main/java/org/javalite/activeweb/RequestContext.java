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

import org.javalite.activeweb.proxy.HttpServletResponseProxy;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is used internally by the framework. Do not interfere for your own good.
 *
 * @author Igor Polevoy
 */
public class RequestContext {

    private static ThreadLocal<HttpServletRequest> request = new ThreadLocal<>();
    private static ThreadLocal<HttpServletResponseProxy> response = new ThreadLocal<>();
    private static ThreadLocal<FilterConfig> filterConfig = new ThreadLocal<>();
    private static ThreadLocal<ControllerResponse> controllerResponse = new ThreadLocal<>();
    private static ThreadLocal<AppContext> appContext = new ThreadLocal<>();
    private static ThreadLocal<RequestVo> requestVo = new ThreadLocal<>();
    private static ThreadLocal<String> format = new ThreadLocal<>();
    private static ThreadLocal<String> encoding = new ThreadLocal<>();
    private static ThreadLocal<Route> route = new ThreadLocal<>();
    private static ThreadLocal<Map<String, Object>> values = new ThreadLocal<>();
    private static ThreadLocal<Boolean> exceptionHappened = new ThreadLocal<>();
    private static ThreadLocal<List<FormItem>> formItems = new ThreadLocal<>();
    private static ThreadLocal<Map<String, String>> params1st = new ThreadLocal<>();


    private RequestContext() {}


    static Map<String, String> getParams1st() {
        return params1st.get();
    }

    static void setParams1st(Map<String, String> params1st) {
        RequestContext.params1st.set(params1st);
    }

    static Map<String, Object> getValues() {
        return values.get();
    }

    static String getEncoding() {
        return encoding.get();
    }

    static void setEncoding(String encoding) {
        RequestContext.encoding.set(encoding);
    }

    static String getFormat() {
        return format.get();
    }

    static void setFormat(String format) {
        RequestContext.format.set(format);
    }

    static RequestVo getRequestVo() {
        return requestVo.get();
    }

    static void setRequestVo(RequestVo requestVo) {
        RequestContext.requestVo.set(requestVo);
    }

    static AppContext getAppContext() {
        return appContext.get();
    }

    static void setAppContext(AppContext appContext) {
        RequestContext.appContext.set(appContext);
    }


    static void setHttpRequest(HttpServletRequest req){
        request.set(req);
    }

    static HttpServletRequest getHttpRequest(){
        return request.get();
    }

    static void setHttpResponse(HttpServletResponse resp){        
        response.set(new HttpServletResponseProxy(resp));
    }

    static HttpServletResponseProxy getHttpResponse(){
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

    public static boolean exceptionHappened() {
        return exceptionHappened.get();
    }

    public static void exceptionDidHappen() {
        exceptionHappened.set(true);
    }

    static FilterConfig getFilterConfig() {
        return filterConfig.get();
    }

    static void setFilterConfig(FilterConfig config) {
        filterConfig.set(config);
    }

    static void setTLs(HttpServletRequest req, HttpServletResponse resp, FilterConfig conf, AppContext context,
                       RequestVo requestVo, String format) {
        setHttpRequest(req);
        setHttpResponse(resp);
        setFilterConfig(conf);
        setAppContext(context);
        setRequestVo(requestVo);
        setFormat(format);
        exceptionHappened.set(false);
    }

    static void setRoute(Route route) {
        if (route == null)
            throw new IllegalArgumentException("Route could not be null");
        if (route.getId() != null){
            getHttpRequest().setAttribute("id", route.getId());
        }

        if(route.isWildCard()){
            requestVo.get().setWildCardName(route.getWildCardName());
            requestVo.get().setWildCardValue(route.getWildCardValue());
        }
        RequestContext.route.set(route);
        RequestContext.values.set(new HashMap<>());
    }

    static List<FormItem> getFormItems() {
        return formItems.get();
    }

    static void setFormItems(List<FormItem> items) {
        formItems.set(items);
    }

    static void clear() {
        request.set(null);
        response.set(null);
        controllerResponse.set(null);
        route.set(null);
        filterConfig.set(null);
        requestVo.set(null);
        format.set(null);
        encoding.set(null);
        appContext.set(null);
        values.set(null);
        exceptionHappened.set(false);
        formItems.set(null);
        params1st.set(null);
    }

    public static boolean isCustomRoute(){
        return RequestContext.getRoute() != null && RequestContext.getRoute().isCustom();
    }
}
