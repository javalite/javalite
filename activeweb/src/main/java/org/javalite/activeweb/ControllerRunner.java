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
import org.javalite.activejdbc.Model;
import org.javalite.json.JSONBase;
import org.javalite.json.JSONHelper;
import org.javalite.json.JSONMap;
import org.javalite.validation.ImplicitConversionValidator;
import org.javalite.validation.Validatable;
import org.javalite.activeweb.annotations.FailedValidationReply;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.activeweb.freemarker.AbstractFreeMarkerConfig;
import org.javalite.activeweb.freemarker.FreeMarkerTemplateManager;
import org.javalite.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
            if (route.actionSupportsHTTPMethod() || route.isCustom()) {
                injectController(route.getController());
                executeAction(route);
            }
        }

        if(injectTags){
            injectFreemarkerTags();
        }

        renderResponse(route);
        processFlash();
    }

    private void executeAction(Route route) {
        try{
            Method m = route.getActionMethod();
            if(!AppController.class.isAssignableFrom(m.getDeclaringClass())){ // see https://github.com/javalite/activeweb/issues/272
                throw new ActionNotFoundException("Cannot execute action '" + route.getActionName() + "' on controller: " + route.getControllerClassName());
            }

            if(route.hasArgument()){
                Object requestValue = getRequestObject(route);
                LOGGER.debug("Executing: " + route.getController() + "#" + route.getActionMethod().getName() + " with argument: " + requestValue.getClass());

                if( requestValue instanceof Validatable){

                    Validatable v =((Validatable)requestValue);
                    v.validate(false);

                    FailedValidationReply autoReply = route.getController().getClass().getAnnotation(FailedValidationReply.class);
                    if(autoReply == null){
                        autoReply = route.getActionMethod().getAnnotation(FailedValidationReply.class);
                    }

                    if(autoReply != null){
                        //TODO: shall we get a Locale for errors() from the HTTP request?
                        DirectResponse dr = new DirectResponse(v.errors().toJSON());
                        dr.setStatus(autoReply.value());
                        dr.setContentType("application/json");
                        RequestContext.setControllerResponse(dr);
                    }
                }

                if(RequestContext.getControllerResponse() == null){
                    m.invoke(route.getController(), requestValue);
                }
            }else {
                LOGGER.debug("Executing: " + route.getController() + "#" + route.getActionMethod().getName());
                m.invoke(route.getController());
            }
        }catch(InvocationTargetException e){
            if(e.getCause() != null && e.getCause() instanceof  WebException){
                throw (WebException)e.getCause();
            }else if(e.getCause() != null && e.getCause() instanceof RuntimeException){
                throw (RuntimeException)e.getCause();
            }else if(e.getCause() != null){
                throw new ControllerException(e.getCause());
            }
        }catch(WebException | ConversionException e){
            throw e;
        } catch(Exception e){
            throw new ControllerException(e);
        }
    }


    @SuppressWarnings("unchecked")
    private Object getRequestObject(Route route) throws IllegalAccessException, InstantiationException, IOException, InvocationTargetException, NoSuchMethodException {
        String contentType = RequestContext.getHttpRequest().getContentType();
        boolean jsonRequest = contentType != null && contentType.toLowerCase().contains("application/json");
        JSONMap requestMap;
        InputStream in = route.getController().getRequestInputStream();
        String requestBody = Util.read(in);
        if(jsonRequest){
            try{
                //TODO: here we presume that the incoming JSON will be an Object, but it can also me an array!
                requestMap = JSONHelper.toMap(requestBody);
            }catch(RuntimeException e){
                throw new ControllerException("Failed to convert JSON request to JSON document", e.getCause());
            }
        }else {
            requestMap = new JSONMap(route.getController().params1st());
        }
        return getObjectWithValues(route, requestMap);
    }

    private Object getObjectWithValues(Route route, JSONMap requestMap) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Class argumentClass = route.getArgumentClass();

        if (JSONBase.class.isAssignableFrom(argumentClass)) {
            return getJSONBase(argumentClass, requestMap);
        }else if (JSONMap.class.equals(argumentClass)) {
            return new JSONMap(requestMap);
        }else {
            Object requestObject = argumentClass.getDeclaredConstructor().newInstance();
            if (requestObject instanceof Model) {
                return ((Model) requestObject).fromMap(requestMap);
            } else {
                return getFilledPOJO(argumentClass, requestMap);
            }
        }
    }



    private Object getJSONBase(Class argumentClass, JSONMap requestMap) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor constructor;
        try {
            constructor = argumentClass.getDeclaredConstructor(Map.class);
            return constructor.newInstance(requestMap);
        } catch (NoSuchMethodException e) {
            throw new ControllerException("Failed to find a constructor in " + argumentClass + " that accepts a Map.");
        }
    }
    private Object getFilledPOJO(Class argumentClass, JSONMap requestMap) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object requestObject = argumentClass.getDeclaredConstructor().newInstance();
        Map translatedRequestMap = translateMapToJava(requestMap);
        Field[] fields = argumentClass.getDeclaredFields();
        for (Field field : fields) {
            setField(field, translatedRequestMap, requestObject);
        }
        return requestObject;
    }


    private void setField(Field field, Map translatedRequestMap, Object requestObject) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        boolean needRevert = false;
        if (!field.isAccessible()) {
            field.setAccessible(true);
            needRevert = true;
        }

        String typeName = field.getType().getName();


        //TODO: cache Convert.toXX methods.
        Object value = translatedRequestMap.get(field.getName());
        if (value != null) {
            if (typeName.equalsIgnoreCase("int") || typeName.contains("Integer")) {
                setField(requestObject, Convert.class.getMethod("toInteger", Object.class), value, field);
            } else if (typeName.equals("double") || typeName.equals(Double.class.getName())) {
                setField(requestObject, Convert.class.getMethod("toDouble", Object.class), value, field);
            } else if (typeName.equals("float") || typeName.equals(Float.class.getName())) {
                setField(requestObject, Convert.class.getMethod("toFloat", Object.class), value, field);
            } else if (typeName.equals("boolean") || typeName.equals(Boolean.class.getName())) {
                setField(requestObject, Convert.class.getMethod("toBoolean", Object.class), value, field);
            } else if (typeName.equals(String.class.getName())) {
                setField(requestObject, Convert.class.getMethod("toString", Object.class), value, field);
            }
        }

        if (needRevert) {
            field.setAccessible(false);
        }

    }


    private void setField(Object requestObject, Method method, Object value, Field field) throws IllegalAccessException, InvocationTargetException {
        try{
            Object convertedValue = method.invoke(requestObject, value);
            field.set(requestObject, convertedValue);

        }catch(InvocationTargetException e){
            if(e.getCause() != null && e.getCause() instanceof ConversionException){
                ConversionException conversionException = (ConversionException) e.getCause();
                if(requestObject instanceof Validatable){
                    ((Validatable)requestObject).addFailedValidator(new ImplicitConversionValidator(conversionException.getMessage()), field.getName());
                }else {
                    throw conversionException;
                }
            }else {
                throw e;
            }
        }
    }

    /**
     * Translates names from underscores and hyphens to Java CamelCase.
     */
    private Map translateMapToJava(JSONMap requestMap) {
        Map<String, Object> translatedMap = new HashMap<>();
        requestMap.keySet().forEach(key -> {
            translatedMap.put(Inflector.camelize(key, false), requestMap.get(key));
        });
        return translatedMap;
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
