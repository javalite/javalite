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
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.activeweb.freemarker.AbstractFreeMarkerConfig;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import org.javalite.common.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

import static org.javalite.common.Util.join;

/**
 * One of the main classes of the framework, responsible for execution of controllers and filters.
 *
 * @author Igor Polevoy
 */
class ControllerRunner {

    private boolean injectTags = Configuration.getTemplateManager() instanceof FreeMarkerTemplateManager;

    private static Logger LOGGER = LoggerFactory.getLogger(ControllerRunner.class);
    private boolean tagsInjected;

    protected void run(Route route) throws Exception {
        Configuration.injectFilters(); //no worries, will execute once, as filters have a life span of the app
        try {
            filterBefore(route);
            executeController(route);
        }catch(ActionNotFoundException e){
            throw e;
        }catch (RuntimeException e) {
            RequestContext.setControllerResponse(null);//must blow away, as this response is not valid anymore.
            RequestContext.exceptionDidHappen();
            if (exceptionHandled(e, route)) {
                LOGGER.debug("A filter has called render(..) method, proceeding to render it...");
                renderResponse(route);//a filter has created an instance of a controller response, need to render it.
            }else{
                throw e;//if exception was not handled by filter, re-throw
            }
        }finally {
            filterAfter(route);
        }
    }

    private void executeController(Route route) throws IllegalAccessException, InstantiationException {
        if (RequestContext.getControllerResponse() == null) {//execute controller... only if a filter did not respond

            String actionMethod = Inflector.camelize(route.getActionName().replace('-', '_'), false);
            if (checkActionMethod(route.getController(), actionMethod) || route.isCustom()) {
                injectController(route.getController());
                LOGGER.debug("Executing: " + route.getController() + "#" + actionMethod);
                executeAction(route.getController(), actionMethod);
            }
        }

        if(injectTags){
            injectFreemarkerTags();
        }

        renderResponse(route);
        processFlash();
    }

    private void executeAction(Object controller, String actionName) {
        try{
            Method m = controller.getClass().getMethod(actionName);
            if(!AppController.class.isAssignableFrom(m.getDeclaringClass())){ // see https://github.com/javalite/activeweb/issues/272
                throw new ActionNotFoundException("Cannot execute action '" + actionName + "' on controller: " + controller);
            }
            m.invoke(controller);
        }catch(InvocationTargetException e){
            if(e.getCause() != null && e.getCause() instanceof  WebException){
                throw (WebException)e.getCause();
            }else if(e.getCause() != null && e.getCause() instanceof RuntimeException){
                throw (RuntimeException)e.getCause();
            }else if(e.getCause() != null){
                throw new ControllerException(e.getCause());
            }
        }catch(WebException e){
            throw e;
        }catch(Exception e){
            throw new ControllerException(e);
        }
    }

    /**
     * Injects FreeMarker tags with dependencies from Guice module.
     */
    private void injectFreemarkerTags() {
        if(!tagsInjected){
            AbstractFreeMarkerConfig freeMarkerConfig = Configuration.getFreeMarkerConfig();

            Injector injector = Configuration.getInjector();
            tagsInjected = true;
            if(injector == null || freeMarkerConfig == null){
                return;
            }
            freeMarkerConfig.inject(injector);
        }
    }

    /**
     * Injects controller with dependencies from Guice module.
     */
    private void injectController(AppController controller) {
        Injector injector = Configuration.getInjector();
        if (injector != null) {
            injector.injectMembers(controller);
        }
    }


    private void renderResponse(Route route) throws InstantiationException, IllegalAccessException {

        //set encoding. Priority: action, then controller
        if (RequestContext.getEncoding() != null) {
            RequestContext.getHttpResponse().setCharacterEncoding(RequestContext.getEncoding());
        } else if (route.getController().getEncoding() != null) {
            RequestContext.getHttpResponse().setCharacterEncoding(route.getController().getEncoding());
        }

        ControllerResponse controllerResponse = RequestContext.getControllerResponse();
        String controllerLayout = route.getController().getLayout();
        if (controllerResponse == null) {
            createDefaultResponse(route, controllerLayout);
        } else if (controllerResponse instanceof RenderTemplateResponse) {
            configureExplicitResponse(route, controllerLayout, (RenderTemplateResponse) controllerResponse);
        }

        controllerResponse = RequestContext.getControllerResponse();
        if (controllerResponse instanceof RenderTemplateResponse) {
            ParamCopy.copyInto((controllerResponse.values()));
            controllerResponse.process();
        }else {
            if(controllerResponse.getContentType() == null){
                controllerResponse.setContentType(route.getController().getContentType());
            }
            controllerResponse.process();
        }
    }

    //this is configuration of explicit response. If render() method was called in controller, we already have instance of
    // response on current thread.
    private void configureExplicitResponse(Route route, String controllerLayout, RenderTemplateResponse resp) throws InstantiationException, IllegalAccessException {
            if(!Configuration.getDefaultLayout().equals(controllerLayout) && resp.hasDefaultLayout()){
                resp.setLayout(controllerLayout);
            }
            if(resp.getContentType() == null){
                resp.setContentType(route.getController().getContentType());
            }
            resp.setTemplateManager(Configuration.getTemplateManager());
    }

    // this is implicit processing - default behavior, really
    private void createDefaultResponse(Route route, String controllerLayout) throws InstantiationException, IllegalAccessException {
           String controllerPath = Router.getControllerPath(route.getController().getClass());
            String template =  controllerPath + "/" + route.getActionName();
            RenderTemplateResponse resp = new RenderTemplateResponse(route.getController().values(), template, RequestContext.getFormat());
            if(!Configuration.getDefaultLayout().equals(controllerLayout)){
                resp.setLayout(controllerLayout);//could be a real layout ot null for no layout
            }
            if(resp.getContentType() == null){
                resp.setContentType(route.getController().getContentType());
            }
            RequestContext.setControllerResponse(resp);
            resp.setTemplateManager(Configuration.getTemplateManager());
    }


    private void processFlash() {
        HttpSession session = RequestContext.getHttpRequest().getSession(false);
        if (session != null) {
            Object flashObj = session.getAttribute("flasher");
            if (flashObj != null && flashObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> flasher = (Map) flashObj;
                if (flasher.get("count") == null) { //just created
                    flasher.put("count", 0);
                } else if (flasher.get("count").equals(0)) {
                    session.removeAttribute("flasher");
                }
            }
        }
    }

    /**
     * Checks if the action method supports requested HTTP method
     */
    private boolean checkActionMethod(AppController controller, String actionMethod) {
        HttpMethod method = HttpMethod.getMethod(RequestContext.getHttpRequest());
        if (!controller.actionSupportsHttpMethod(actionMethod, method)) {
            DirectResponse res = new DirectResponse("");
            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
            res.setStatus(405);
            LOGGER.warn("Requested action does not support HTTP method: " + method.name() + ", returning status code 405.");
            RequestContext.setControllerResponse(res);

            //TODO: candidate for caching below, list of allowed HTTP methods
            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
            RequestContext.getHttpResponse().setHeader("Allow", join(controller.allowedActions(actionMethod), ", "));
            return false;
        }
        return true;
    }

    private boolean exceptionHandled(Exception e, Route route) throws Exception{
        for(HttpSupportFilter filter: Configuration.getFilters()){
            if(Configuration.getFilterMetadata(filter).matches(route)){
                LOGGER.debug("Executing filter: " + filter.getClass().getName() + "#exceptionHandled");
                filter.onException(e);
                if (RequestContext.getControllerResponse() != null){ // this filter sent a response, breaking the loop
                    break;
                }
            }
        }
        return RequestContext.getControllerResponse() != null;
    }

    /**
     * Will execute <code>before()</code> method of every filter in order of definition.
     * Will break the cycle if a filter produced a controller response.
     *
     * @param route current route
     */
    private void filterBefore(Route route) {
        try {

            for(HttpSupportFilter filter: Configuration.getFilters()){
                if(Configuration.getFilterMetadata(filter).matches(route)){
                    LOGGER.debug("Executing filter: " + filter.getClass().getName() + "#before");
                    filter.before();
                }else{
                    LOGGER.debug("Filter: " + filter.getClass().getName() + "#before  does not match route: " + route );
                }
                if (RequestContext.getControllerResponse() != null){
                    LOGGER.debug("Filter: " + filter.getClass().getName() + "#before  responded to client. Breaking filter chain for route:  " + route );
                    return;//a filter responded, no need to run other filters!
                }
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new FilterException(e);
        }
    }

    /**
     * Run filters in opposite order
     */
    private void filterAfter(Route route) {
        try {
            List<HttpSupportFilter> filters = Configuration.getFilters();
            for (int i = filters.size() - 1; i >= 0; i--) {
                HttpSupportFilter filter = filters.get(i);
                if(Configuration.getFilterMetadata(filter).matches(route)){
                    LOGGER.debug("Executing filter: " + filter.getClass().getName() + "#after");
                    filters.get(i).after();
                }else {
                    LOGGER.debug("Filter: " + filter.getClass().getName() + "#after does not match route: " + route );
                }
            }
        } catch (Exception e) {
            throw new FilterException(e);
        }
    }
}
