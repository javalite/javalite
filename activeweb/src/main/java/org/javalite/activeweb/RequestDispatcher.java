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

import freemarker.template.TemplateNotFoundException;
import org.javalite.activejdbc.DB;

import org.javalite.activeweb.proxy.ProxyWriterException;
import org.javalite.activeweb.proxy.ProxyIOException;
import org.javalite.activeweb.proxy.HttpServletResponseProxy;
import org.javalite.app_config.AppConfig;
import org.javalite.common.Convert;
import org.javalite.json.JSONHelper;
import org.javalite.common.Util;
import org.javalite.logging.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.javalite.common.Collections.map;
import static org.javalite.common.Util.getCauseMessage;
import static org.javalite.json.JSONHelper.toJSON;

/**
 * @author Igor Polevoy
 */
public class RequestDispatcher implements Filter {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private FilterConfig filterConfig;
    private List<String> exclusions = new ArrayList<>();
    private ControllerRunner runner = new ControllerRunner();
    private AppContext appContext;
    private Bootstrap appBootstrap;
    private String encoding;

    private static ThreadLocal<Long> time = new ThreadLocal<>();

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        HttpMethod.disableMethodSimulation(Convert.toBoolean(filterConfig.getInitParameter("disable_method_simulation")));

        Configuration.getTemplateManager().setServletContext(filterConfig.getServletContext());
        appContext = new AppContext();
        filterConfig.getServletContext().setAttribute("appContext", appContext);

        String exclusionsParam = filterConfig.getInitParameter("exclusions");
        if (exclusionsParam != null) {
            exclusions.addAll(Arrays.asList(exclusionsParam.split(",")));
            for (int i = 0; i < exclusions.size(); i++) {
                exclusions.set(i, exclusions.get(i).trim());
            }
        }
        initApp(appContext);
        encoding = filterConfig.getInitParameter("encoding");
        logger.info("ActiveWeb: starting the app in environment: " + AppConfig.activeEnv());
    }

    protected void initApp(AppContext context){
        initAppConfig(Configuration.getDbConfigClassName(), context, false);

        initAppConfig(Configuration.getBootstrapClassName(), context, true);
        //these are optional config classes:

        initAppConfig(Configuration.getControllerConfigClassName(), context, false);
    }

    public AppContext getContext() {
        return appContext;
    }

    //this exists for testing only
    private AbstractRouteConfig routeConfigTest;
    private boolean testMode;
    protected void setRouteConfig(AbstractRouteConfig routeConfig) {
        this.routeConfigTest = routeConfig;
        testMode = true;
    }

    private Router getRouter(AppContext context){
        String routeConfigClassName = Configuration.getRouteConfigClassName();
        Router router = new Router(filterConfig.getInitParameter("root_controller"));
        AbstractRouteConfig routeConfigLocal;
        try {
            if(testMode){
                routeConfigLocal = routeConfigTest;
            }else{
                Class configClass = DynamicClassFactory.getCompiledClass(routeConfigClassName);
                routeConfigLocal = (AbstractRouteConfig) configClass.getDeclaredConstructor().newInstance();
            }
            routeConfigLocal.clear();
            routeConfigLocal.init(context);
            router.setRoutes(routeConfigLocal.getRoutes());
            router.setIgnoreSpecs(routeConfigLocal.getIgnoreSpecs());
            router.setStrictMode(routeConfigLocal.isStrictMode());

            logger.debug("Loaded routes from: " + routeConfigClassName);

        } catch (IllegalArgumentException | ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            logger.debug("Did not find custom routes. Going with built in defaults: " + getCauseMessage(e));
        }
        return router;
    }

    private void initAppConfig(String configClassName, AppContext context, boolean fail){
        InitConfig initConfig;
        try {
            Class<?> c = Class.forName(configClassName);
            initConfig = (InitConfig) c.getDeclaredConstructor().newInstance();
            initConfig.init(context);
            if(initConfig instanceof  Bootstrap){
                appBootstrap = (Bootstrap) initConfig;
                if (!Configuration.isTesting()) {
                    Configuration.setInjector(appBootstrap.getInjector());
                }
            }
            initConfig.completeInit();
        }
        catch (Throwable e) {
            if(fail){
                logger.error("Failed to create and init a new instance of class: " +
                        configClassName + ". Application failed to start, so it will not run.", e);
                if(e.getCause() != null){
                    logger.error("Cause exception below: ", e.getCause());
                }
                throw new InitException(e);
            }else{
                logger.warn("Failed to init a class name: " + configClassName + ", proceeding without it.");
            }
        }
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        try {

            time.set(System.currentTimeMillis());

            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) resp;

            if(encoding != null){
                logger.debug("Setting encoding: " + encoding);
                request.setCharacterEncoding(encoding);
                response.setCharacterEncoding(encoding);
            }

            String path = request.getServletPath();

            if(excluded(path)){
                chain.doFilter(req, resp);
                logger.debug("URI excluded: " + path);
                return;
            }

            String format = null;
            String uri;
            if(path.contains(".")){
                uri = path.substring(0, path.lastIndexOf('.'));
                format = path.substring(path.lastIndexOf('.') + 1);
            }else{
                uri = path;
            }

            RequestContext.setTLs(request, response, filterConfig, appContext, new RequestVo(), format);
            if (Util.blank(uri)) {
                uri = "/";//different servlet implementations, damn.
            }

            Router router = getRouter(appContext);
            Route route;
            try{
                route = router.recognize(uri, HttpMethod.getMethod(request));
            }catch(IllegalArgumentException e){
                throw  new RouteException("Method not supported: " + request.getMethod());
            }


            if (route != null && route.ignores(path)) {
                chain.doFilter(req, resp);
                logger.debug("URI ignored: " + path);
                return;
            }

            if (route != null) {
                RequestContext.setRoute(route);
                if (Configuration.logRequestParams()) {
                    logger.info(toJSON("info","executing controller",
                            "controller", route.getControllerClassName(),
                            "action", route.getActionName(),
                            "method", route.getHttpMethod()));
                }
                runner.run(route);
                logDone(null);
            } else {
                logger.warn("No matching route for servlet path: " + request.getServletPath() + ", passing down to container.");
                chain.doFilter(req, resp);//let it fall through
            }
        }catch (CompilationException
                 | ClassLoadException
                 | ActionNotFoundException
                 | ViewMissingException
                 | RouteException e) {
            renderSystemError(404, e);
        } catch (Throwable e) {
            if(e.getClass().equals(ProxyWriterException.class)
                    || e.getCause() != null && e.getCause().getClass().equals(ProxyIOException.class)){
                RequestContext.getHttpResponse().setStatus(499);// side effect :(
                logDone(e);
            }else{
                renderSystemError(500, e);
            }
        }finally {
            RequestContext.clear();
            Context.clear();
            List<String> connectionsRemaining = DB.getCurrrentConnectionNames();
            if(!connectionsRemaining.isEmpty()){
                logger.warn("CONNECTION LEAK DETECTED ... and AVERTED!!! You left connections opened:"
                        + connectionsRemaining + ". ActiveWeb is closing all active connections for you...");
                DB.closeAllConnections();
            }
        }
    }

    private boolean excluded(String servletPath) {
        for (String exclusion : exclusions) {
            if (servletPath.contains(exclusion))
                return true;
        }
        return false;
    }

    private void renderSystemError(int status, Throwable e) {

        if(status != 404){
            logger.error("Rendering error", e);
        }

        try{
            ErrorRouteBuilder builder = Configuration.getErrorRouteBuilder();
            if(builder != null){

                Route r = builder.getRoute(e);
                RequestContext.setRoute(r); // a little hacky :(
                runner.run(r);
                if(status == 404) {
                    RequestContext.getHttpResponse().setStatus(404);
                    logDone(null);
                }else {
                    logDone(e);
                }
            }else{
                sendDefaultResponse(status, e);
            }
        }catch(Throwable t){
            logger.error("ActiveWeb internal error: ", t);
            try{
                if(t instanceof  ActionNotFoundException
                        || t instanceof TemplateNotFoundException){
                    writeBack("resource not found", 404);
                }else{
                    writeBack("internal error", 500);
                }
            }catch(Exception ex){
                logger.error("Exception trying to render error response", ex);
                logger.error("Original error", t);
            }
        }
    }

    private void writeBack(String message, int status) throws IOException {
        HttpServletResponseProxy httpServletResponseProxy = RequestContext.getHttpResponse();
        if(httpServletResponseProxy == null){
            throw new WebException("Catastrophic failure: failed to find HttpServletResponse...");
        }

        httpServletResponseProxy.setStatus(status);
        HttpServletResponseProxy.OutputType outputType = httpServletResponseProxy.getOutputType();
        if(outputType == HttpServletResponseProxy.OutputType.OUTPUT_STREAM
                || outputType == HttpServletResponseProxy.OutputType.NONE){
            ServletOutputStream outputStream = httpServletResponseProxy.getOutputStream();
            if(outputStream == null){
                throw new WebException("Catastrophic failure: failed to find OutputStream...");
            }else{
                outputStream.print(message); // "internal error"
                outputStream.flush();

            }
        }else if(HttpServletResponseProxy.OutputType.WRITER == outputType){
            PrintWriter writer = httpServletResponseProxy.getWriter();
            if(writer == null){
                throw new WebException("Catastrophic failure: failed to find Writer...");
            }else{
                writer.print(message);
                writer.flush();
            }
        }
    }

    private void sendDefaultResponse(int status, Throwable e) {
        RequestContext.getHttpResponse().setStatus(status);
        logDone(e);

        HttpServletRequest req = RequestContext.getHttpRequest();
        String requestedWith = req.getHeader("x-requested-with") == null ?
                req.getHeader("X-Requested-With") : req.getHeader("x-requested-with");

        if (requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest")) {
            try {

                RequestContext.getHttpResponse().getWriter().write(Util.getStackTraceString(e));
            } catch (Exception ex) {
                logger.error("Failed to send error response to client", ex);
            }
        } else {

            String message = status == 404 ? "resource not found" : "server error";

            DirectResponse directResponse;
            if ("application/json".equals(RequestContext.getHttpRequest().getContentType())) {
                directResponse = new DirectResponse("""
                            {"message":"%s"}""".formatted(message));
                RequestContext.getHttpResponse().setContentType("application/json");
            } else {
                directResponse = new DirectResponse(message);
                RequestContext.getHttpResponse().setContentType("text/plain");
            }

            directResponse.setStatus(status);
            directResponse.process();
        }
    }

    private void logDone(Throwable throwable) {
        long millis = System.currentTimeMillis() - time.get();
        int status = RequestContext.getHttpResponse().getStatus();
        Route route = RequestContext.getRoute();
        String controller = route == null ? "" : route.getControllerClassName();
        String action = route == null ? "" : route.getActionName();
        String method = RequestContext.getHttpRequest().getMethod();
        String url = RequestContext.getHttpRequest().getRequestURL().toString();

        ControllerResponse cr = RequestContext.getControllerResponse();

        String redirectTarget = null;
        if(cr instanceof RedirectResponse){
            RedirectResponse rr = (RedirectResponse) cr;
            redirectTarget = rr.redirectValue();
        }

        Map<String, Object> log = map(
                "controller", controller,
                "action", action,
                "duration_millis", millis,
                "method", method,
                "url", url,
                "remote_ip", getRemoteIP(),
                "status", status);

        if (redirectTarget != null) {
            log.put("redirect_target", redirectTarget);
            if(RequestContext.getValues().size() > 0){
                log.put("WARNING", "You passed values to a view and redirected! Are you sure you know what you are doing?");
            }
        }

        if (throwable != null) {
            String errorMessage = status == 404 ? "Route not found" :
                JSONHelper.sanitize(throwable.getMessage() != null ? throwable.getMessage() : throwable.toString());
            log.put("error", errorMessage);
        }

        //usage of the side effect: The status code is used to add a specific message to the log.
        if(RequestContext.getHttpResponse().getStatus() == 499){
         log.put("message", "Looks like the client abandoned this request...");
        }

        addRequestHeaders(log);

        if(throwable != null && status >= 500){
            logger.error(toJSON(log), throwable);
        }else if(throwable != null && status == 404) {
            logger.info(toJSON(log));
        }else if(throwable != null && status == 499) {
            logger.warn(toJSON(log), throwable.toString());
        } else {
            logger.info(toJSON(log));
        }
    }

    private void addRequestHeaders(Map<String, Object> log) {
        List<String> logHeaders = Configuration.getLogHeaders();
        Enumeration<String> requestHeaders = RequestContext.getHttpRequest().getHeaderNames();
        Map<String, String> headersMap = null;
        while (requestHeaders.hasMoreElements()) {
            String header = requestHeaders.nextElement();
            if(logHeaders.contains(header)){
                if(headersMap == null){
                    headersMap = new HashMap<>();
                    log.put("headers", headersMap);
                }
                headersMap.put(header, JSONHelper.sanitize(RequestContext.getHttpRequest().getHeader(header)));
            }
        }
    }

    private String getRemoteIP() {
        String h = RequestContext.getHttpRequest().getHeader("X-Forwarded-For");
        return !Util.blank(h) ? h : RequestContext.getHttpRequest().getRemoteAddr();
    }

    public void destroy() {
        if(appBootstrap != null){ // failed start?
            if (!AppConfig.isInTestMode()){
                appBootstrap.destroy();
            }else{
                logger.warn("Omitting destruction of "  + appBootstrap + " in tests");
            }

            appBootstrap.destroy(appContext);
        }
    }
}
