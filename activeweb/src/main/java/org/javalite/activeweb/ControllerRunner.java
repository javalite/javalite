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

import org.javalite.activeweb.controller_filters.ControllerFilter;
import com.google.inject.Injector;
import org.javalite.activeweb.freemarker.AbstractFreeMarkerConfig;
import org.javalite.common.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(ControllerRunner.class.getName());
    private boolean tagsInjected = false;

    protected void run(Route route, boolean integrateViews) throws Exception {
        ControllerRegistry controllerRegistry = Context.getControllerRegistry();
        List<ControllerRegistry.FilterList> globalFilterLists = controllerRegistry.getGlobalFilterLists();
        List<ControllerFilter> controllerFilters = controllerRegistry.getMetaData(route.getController().getClass()).getFilters(route.getActionName());

        Context.getControllerRegistry().injectFilters(); //will execute once, really filters are persistent

        try {
            filterBefore(route, globalFilterLists, controllerFilters);

            if (Context.getControllerResponse() == null) {//execute controller... only if a filter did not respond

                String actionMethod = Inflector.camelize(route.getActionName().replace('-', '_'), false);
                if (checkActionMethod(route.getController(), actionMethod)) {
                    //Configuration.getTemplateManager().
                    injectController(route.getController());
                    if(Configuration.logRequestParams()){
                        logger.info("Executing controller: " + route.getController().getClass().getName() + "." + actionMethod);
                    }
                    executeAction(route.getController(), actionMethod);
                }
            }

            injectFreemarkerTags();
            renderResponse(route, integrateViews);
            processFlash();

            //run filters in opposite order
            filterAfter(route, globalFilterLists, controllerFilters);
        }
        catch(ActionNotFoundException e){
            throw e;
        }
        catch (RuntimeException e) {
            Context.setControllerResponse(null);//must blow away, as this response is not valid anymore.

            if (exceptionHandled(e, route, globalFilterLists, controllerFilters)) {
                logger.debug("A filter has called render(..) method, proceeding to render it...");
                renderResponse(route, integrateViews);//a filter has created an instance of a controller response, need to render it.
            }else{
                throw e;//if exception was not handled by filter, re-throw
            }
        }
    }

    /**
     * Injects FreeMarker tags with dependencies from Guice module.
     */
    private void injectFreemarkerTags() {
        if(!tagsInjected){
            AbstractFreeMarkerConfig freeMarkerConfig = Configuration.getFreeMarkerConfig();

            Injector injector = Context.getControllerRegistry().getInjector();
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
        Injector injector = Context.getControllerRegistry().getInjector();
        if (injector != null) {
            injector.injectMembers(controller);
        }
    }


    private void renderResponse(Route route,  boolean integrateViews) throws InstantiationException, IllegalAccessException {

        //set encoding. Priority: action, then controller
        if (Context.getEncoding() != null) {
            Context.getHttpResponse().setCharacterEncoding(Context.getEncoding());
        } else if (route.getController().getEncoding() != null) {
            Context.getHttpResponse().setCharacterEncoding(route.getController().getEncoding());
        }

        ControllerResponse controllerResponse = Context.getControllerResponse();
        String controllerLayout = route.getController().getLayout();
        if (controllerResponse == null) {
            createDefaultResponse(route, controllerLayout);
        } else if (controllerResponse instanceof RenderTemplateResponse) {
            configureExplicitResponse(route, controllerLayout, (RenderTemplateResponse) controllerResponse);
        }

        controllerResponse = Context.getControllerResponse();
        if (integrateViews && controllerResponse instanceof RenderTemplateResponse) {
            ParamCopy.copyInto((controllerResponse.values()));
            controllerResponse.process();
        }else if(!(controllerResponse instanceof RenderTemplateResponse)){
            if(controllerResponse.getContentType() == null){
                controllerResponse.setContentType(route.getController().getContentType());
            }
            controllerResponse.process();
        }
    }

    //this is configuration of explicit response. If render() method was called in controller, we already have instance of
    // response on current thread.
    private void configureExplicitResponse(Route route, String controllerLayout, RenderTemplateResponse resp) throws InstantiationException, IllegalAccessException {
            String responseLayout = resp.getLayout();
            if(!Configuration.getDefaultLayout().equals(controllerLayout) && Configuration.getDefaultLayout().equals(responseLayout)){
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
            RenderTemplateResponse resp = new RenderTemplateResponse(route.getController().values(), template, Context.getFormat());
            if(!Configuration.getDefaultLayout().equals(controllerLayout)){
                resp.setLayout(controllerLayout);//could be a real layout ot null for no layout
            }
            if(resp.getContentType() == null){
                resp.setContentType(route.getController().getContentType());
            }
            Context.setControllerResponse(resp);
            resp.setTemplateManager(Configuration.getTemplateManager());
    }


    private void processFlash() {
        HttpSession session = Context.getHttpRequest().getSession(false);
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

    private boolean checkActionMethod(AppController controller, String actionMethod) {
        HttpMethod method = HttpMethod.getMethod(Context.getHttpRequest());
        if (!controller.actionSupportsHttpMethod(actionMethod, method)) {
            DirectResponse res = new DirectResponse("");
            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
            res.setStatus(405);
            logger.warn("Requested action does not support HTTP method: " + method.name() + ", returning status code 405.");
            Context.setControllerResponse(res);
            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
            Context.getHttpResponse().setHeader("Allow", join(controller.allowedActions(actionMethod), ", "));
            return false;
        }
        return true;
    }

    private boolean exceptionHandled(Exception e, Route route, List<ControllerRegistry.FilterList> globalFilterLists, List<ControllerFilter> ... filterGroups) throws Exception{

        //first, process global filters and account for exceptions
        for (ControllerRegistry.FilterList filterList : globalFilterLists) {
            if (!filterList.excludesController(route.getController())) {
                List<ControllerFilter> filters = filterList.getFilters();
                for (ControllerFilter controllerFilter : filters) {
                    controllerFilter.onException(e);
                }
            }
        }

        for(List<ControllerFilter> filterGroup: filterGroups){
            for (ControllerFilter controllerFilter : filterGroup) {
                controllerFilter.onException(e);
            }
        }
        return Context.getControllerResponse() != null;
    }

    private void filterBefore(Route route, List<ControllerRegistry.FilterList> globalFilterLists, List<ControllerFilter>... filterGroups) {
        try {

            //first, process global filters and account for exceptions
            for (ControllerRegistry.FilterList filterList : globalFilterLists) {
                if(!filterList.excludesController(route.getController())){
                    List<ControllerFilter> filters = filterList.getFilters();
                    for (ControllerFilter controllerFilter : filters) {
                        controllerFilter.before();
                    }
                }
            }

            //then process all other filters
            for (List<ControllerFilter> filterGroup : filterGroups) {
                for (ControllerFilter controllerFilter : filterGroup) {
                    if (Configuration.logRequestParams()) {
                        logger.debug("Executing filter: " + controllerFilter.getClass().getName() + "#before");
                    }
                    controllerFilter.before();
                    if (Context.getControllerResponse() != null) return;//a filter responded!
                }
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new FilterException(e);
        }
    }

    private void filterAfter(Route route, List<ControllerRegistry.FilterList> globalFilterLists, List<ControllerFilter>... filterGroups) {
        try {

            //first, process global filters and account for exceptions
            for (ControllerRegistry.FilterList filterList : globalFilterLists) {
                if(!filterList.excludesController(route.getController())){
                    List<ControllerFilter> filters = filterList.getFilters();
                    for (ControllerFilter controllerFilter : filters) {
                        controllerFilter.after();
                    }
                }
            }

            for (List<ControllerFilter> filterGroup : filterGroups) {
                for (int i = filterGroup.size() - 1; i >= 0; i--) {
                    if(Configuration.logRequestParams()){
                        logger.debug("Executing filter: " + filterGroup.get(i).getClass().getName() + "#after" );
                    }
                    filterGroup.get(i).after();
                }
            }
        } catch (Exception e) {
            throw  new FilterException(e);
        }
    }

    private void executeAction(Object controller, String actionName) {
        try{
            Method m = controller.getClass().getMethod(actionName);
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
}
