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
import org.javalite.common.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;


/**
 * One of the main classes of the framework, responsible for execution of controllers and filters.
 * 
 * @author Igor Polevoy
 */
class ControllerRunner {

    private static Logger logger = LoggerFactory.getLogger(ControllerRunner.class.getName());

    protected void run(Route route, boolean integrateViews) throws Exception {
        ControllerRegistry controllerRegistry = Context.getControllerRegistry();
        List<ControllerFilter> globalFilters = controllerRegistry.getGlobalFilters();
        List<ControllerFilter> controllerFilters = controllerRegistry.getMetaData(route.getController().getClass()).getFilters();
        List<ControllerFilter> actionFilters = controllerRegistry.getMetaData(route.getController().getClass()).getFilters(route.getActionName());        
        Context.getControllerRegistry().injectFilters();

        try {
            filterBefore(globalFilters, controllerFilters, actionFilters);

            if (Context.getControllerResponse() == null) {//execute controller... only if a filter did not respond

                String methodName = Inflector.camelize(route.getActionName().replace('-', '_'), false);
                checkActionMethod(route.getController(), methodName);
                inject(route.getController());
                if(Configuration.logRequestParams()){
                    logger.info("Executing controller: " + route.getController().getClass().getName() + "." + methodName);
                }

                executeAction(route.getController(), methodName);
            }
            renderResponse(route, integrateViews);
            processFlash();

            //run filters in opposite order
            filterAfter(actionFilters, controllerFilters, globalFilters);
        }
        catch(ActionNotFoundException e){
            throw e;
        }
        catch (WebException e) {
            Context.setControllerResponse(null);//must blow away, as this response is not valid anymore.

            if (exceptionHandled(e, globalFilters, controllerFilters, actionFilters)) {
                logger.debug("A filter has called render(..) method, proceeding to render it...");
                renderResponse(route, integrateViews);//a filter has created an instance of a controller response, need to render it.
            }else{
                throw e;//if exception was not handled by filter, re-throw
            }
        }
    }

    private void inject(AppController controller) {
        Injector injector = Context.getControllerRegistry().getInjector();
        if (injector != null) {
            injector.injectMembers(controller);
        }
    }


    private void renderResponse(Route route,  boolean integrateViews) throws InstantiationException, IllegalAccessException {

        //TODO: a bit of spaghetti code here...
        ControllerResponse controllerResponse = Context.getControllerResponse();
        String controllerLayout = route.getController().getLayout();
        if (controllerResponse == null) {//this is implicit processing - default behavior, really

            String controllerPath = Router.getControllerPath(route.getController().getClass());
            String template =  controllerPath + "/" + route.getActionName();

            RenderTemplateResponse resp = new RenderTemplateResponse(route.getController().values(), template);
    
            if(!Configuration.getDefaultLayout().equals(controllerLayout)){
                resp.setLayout(controllerLayout);//could be a real layout ot null for no layout
            }
            if(resp.getContentType() == null){
                resp.setContentType(route.getController().getContentType());
            }


            Context.setControllerResponse(resp);
            resp.setTemplateManager(Configuration.getTemplateManager());
        } else if (controllerResponse instanceof RenderTemplateResponse) {
            RenderTemplateResponse resp = (RenderTemplateResponse) controllerResponse;

            String responseLayout = resp.getLayout();
            if(!Configuration.getDefaultLayout().equals(controllerLayout) && Configuration.getDefaultLayout().equals(responseLayout)){
                resp.setLayout(controllerLayout);
            }
            if(resp.getContentType() == null){
                resp.setContentType(route.getController().getContentType());
            }
            resp.setTemplateManager(Configuration.getTemplateManager());
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


    private void processFlash() {
        HttpSession session = Context.getHttpRequest().getSession(false);
        if (session != null) {
            Object flashObj = session.getAttribute("flasher");
            if (flashObj != null && flashObj instanceof Map) {
                Map flasher = (Map) flashObj;
                if (flasher.get("count") == null) { //just created
                    flasher.put("count", 0);
                } else if (flasher.get("count").equals(0)) {
                    session.removeAttribute("flasher");
                }
            }
        }
    }

    private void checkActionMethod(AppController controller, String methodName){

        HttpMethod actionHttpMethod = controller.getActionHttpMethod(methodName);

        String requestMethod = HttpMethod.getMethod(Context.getHttpRequest()).toString();
        String actionMethod = actionHttpMethod.toString();
        
        if(!requestMethod.equalsIgnoreCase(actionMethod)){
            throw new ControllerException("Cannot access action " + controller.getClass().getName() + "." + methodName + " with HTTP method: '" + requestMethod +
                    "' because it is configured for method: '" + actionMethod + "'");
        }
    }

    private boolean exceptionHandled(Exception e, List<ControllerFilter> ... filters) throws Exception{
        for(List<ControllerFilter> filterGroup: filters){
            for (ControllerFilter controllerFilter : filterGroup) {
                controllerFilter.onException(e);
            }
        }
        return Context.getControllerResponse() != null;
    }

    private void filterBefore(List<ControllerFilter> ... filters) {
        try{
            for(List<ControllerFilter> filterGroup: filters){
            for (ControllerFilter controllerFilter : filterGroup) {
                if(Configuration.logRequestParams()){
                    logger.debug("Executing filter: " + controllerFilter.getClass().getName() + "#before" );
                }
                controllerFilter.before();
                if(Context.getControllerResponse() != null) return;//a filter responded!
            }
        }
        }catch(Exception e){
            throw new FilterException(e);
        }
    }

    private void filterAfter(List<ControllerFilter>... filters) {
        try {
            for (List<ControllerFilter> filterGroup : filters) {
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
