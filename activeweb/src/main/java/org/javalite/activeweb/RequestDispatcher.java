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

import org.javalite.activejdbc.DB;

import org.javalite.app_config.AppConfig;
import org.javalite.common.JsonHelper;
import org.javalite.common.Util;
import org.javalite.logging.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.javalite.activeweb.Configuration.getDefaultLayout;
import static org.javalite.activeweb.Configuration.useDefaultLayoutForErrors;
import static org.javalite.common.Collections.map;

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
        initAppConfig(Configuration.getBootstrapClassName(), context, true);
        //these are optional config classes:
        initAppConfig(Configuration.getControllerConfigClassName(), context, false);
        initAppConfig(Configuration.getDbConfigClassName(), context, false);
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

            logger.debug("Loaded routes from: " + routeConfigClassName);

        } catch (IllegalArgumentException e) {
            throw e;
        }catch(ConfigurationException e){
            throw  e;
        } catch (Exception e) {
            logger.debug("Did not find custom routes. Going with built in defaults: " + getCauseMessage(e));
        }
        return router;
    }

    //TODO: refactor to some util class. This is stolen...ehrr... borrowed from Apache ExceptionUtils
    static String getCauseMessage(Throwable throwable) {
        List<Throwable> list = new ArrayList<>();
        while (throwable != null && list.contains(throwable) == false) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list.get(0).getMessage();
    }

    private void initAppConfig(String configClassName, AppContext context, boolean fail){
        InitConfig initConfig;
        try {
            Class c = Class.forName(configClassName);
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
                logger.error("Failed to create and init a new instance of class: " + configClassName + ". Application failed to start, so it will not run.");
                throw new InitException(e);
            }else{
                logger.info("Did not find a class named: " + configClassName + ", proceeding without it.");
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


            if(route != null && route.ignores(path)){
                chain.doFilter(req, resp);
                logger.debug("URI ignored: " + path);
                return;
            }

            if (route != null) {
                RequestContext.setRoute(route);
                if (Configuration.logRequestParams()) {
                    logger.info("{\"info\":\"executing controller\",\"controller\":\"" + route.getController().getClass().getName()
                            + "\",\"action\":\""     + route.getActionName()
                            + "\",\"method\":\""     + route.getMethod()
                            + "\"}");
                }
                runner.run(route);
                logDone(null);
            } else {
                //TODO: theoretically this will never happen, because if the route was not excluded, the router.recognize() would throw some kind
                // of exception, leading to the a system error page.
                logger.warn("No matching route for servlet path: " + request.getServletPath() + ", passing down to container.");
                chain.doFilter(req, resp);//let it fall through
            }
        } catch (CompilationException e) {
            renderSystemError(e);
        } catch (ClassLoadException | ActionNotFoundException | ViewMissingException | RouteException e) {
            renderSystemError("/system/404", useDefaultLayoutForErrors() ? getDefaultLayout():null, 404, e);
        } catch (Throwable e) {
            renderSystemError("/system/error", useDefaultLayoutForErrors() ? getDefaultLayout():null, 500, e);
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

    private Map getMapWithExceptionDataAndSession(Throwable e) {
        return map("message", e.getMessage() == null ? e.toString() : e.getMessage(),
                "stack_trace", Util.getStackTraceString(e),
                "session", SessionHelper.getSessionAttributes());
    }


    private boolean excluded(String servletPath) {
        for (String exclusion : exclusions) {
            if (servletPath.contains(exclusion))
                return true;
        }
        return false;
    }


    private void renderSystemError(Throwable e) {
        renderSystemError("/system/error", null, 500, e);
    }


    private void renderSystemError(String template, String layout, int status, Throwable e) {
        try{

//            Map info = map("request_properties", JsonHelper.toJsonString(RequestUtils.getRequestProperties()),
//                            "request_headers" , JsonHelper.toJsonString(RequestUtils.headers()));

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
                RenderTemplateResponse resp = new RenderTemplateResponse(getMapWithExceptionDataAndSession(e), template, null);
                resp.setLayout(layout);
                resp.setContentType("text/html");
                resp.setStatus(status);
                resp.setTemplateManager(Configuration.getTemplateManager());
                ParamCopy.copyInto(resp.values());
                resp.process();
            }
        }catch(Throwable t){

            if(t instanceof IllegalStateException){
                logger.error("Failed to render a template: '" + template + "' because templates are rendered with Writer, but you probably already used OutputStream", t);
            }else{
                logger.error("ActiveWeb internal error: ", t);
            }
            try{
                HttpServletResponseProxy httpServletResponseProxy = RequestContext.getHttpResponse();
                if(httpServletResponseProxy == null){
                    throw new WebException("Catastrophic failure: failed to find HttpServletResponse...", t);
                }

                HttpServletResponseProxy.OutputType outputType = httpServletResponseProxy.getOutputType();
                if(HttpServletResponseProxy.OutputType.OUTPUT_STREAM == outputType){
                    ServletOutputStream outputStream = httpServletResponseProxy.getOutputStream();
                    if(outputStream == null){
                        throw new WebException("Catastrophic failure: failed to find OutputStream...", t);
                    }else{
                        outputStream.print("<div style='color:red'>internal error</div>");
                    }
                }else{
                    PrintWriter writer = httpServletResponseProxy.getWriter();
                    if(writer == null){
                        throw new WebException("Catastrophic failure: failed to find Writer...", t);
                    }else{
                        writer.print("<div style='color:red'>internal error</div>");
                    }
                }
                httpServletResponseProxy.setStatus(500);
            }catch(Exception ex){
                logger.error("Exception trying to render error response", ex);
                logger.error("Original error", t);
            }
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
        }

        if (throwable != null) {
            log.put("error", JsonHelper.sanitize(throwable.getMessage() != null ? throwable.getMessage() : throwable.toString()));
        }

        addRequestHeaders(log);

        if(throwable != null && status >= 500){
            logger.error(JsonHelper.toJsonString(log), throwable);
        }else {
            logger.info(JsonHelper.toJsonString(log));
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
                headersMap.put(header, JsonHelper.sanitize(RequestContext.getHttpRequest().getHeader(header)));
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
