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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
@SuppressWarnings("unchecked")
class ParamCopy {
    private static Logger logger = LoggerFactory.getLogger(ParamCopy.class.getSimpleName());


    static void copyInto(Map assigns){
        insertActiveWebParamsInto(assigns);
        copyRequestAttributesInto(assigns);
        copyRequestParamsInto(assigns);
        copySessionAttrsInto(assigns);
        copyRequestProperties(assigns);
    }

    private static void insertActiveWebParamsInto(Map assigns) {
        assigns.put("context_path", Context.getHttpRequest().getContextPath());
        //in some cases the Route is missing - for example, when exception happened before Router was invoked.
        Route route = Context.getRoute();

        Map params = map("environment", Configuration.getEnv());

        if(route != null){
            params.put("controller", Context.getRoute().getControllerPath());
            params.put("action", Context.getRoute().getActionName());
            params.put("restful", Context.getRoute().getController().restful());
        }
        assigns.put("activeweb", params);
    }


    private static void copySessionAttrsInto(Map assigns) {

        Map<String, Object> sessionAttrs = SessionHelper.getSessionAttributes();
        if (assigns.get("session") != null) {
            logger.warn("found 'session' value set by controller. It is reserved by ActiveWeb and will be overwritten.");
        }
        if (sessionAttrs.containsKey("flasher")){ //flasher is special
            assigns.put("flasher", sessionAttrs.get("flasher"));
        }
        assigns.put("session", sessionAttrs);
    }


    private static void copyRequestParamsInto(Map assigns) {
        Enumeration names = Context.getHttpRequest().getParameterNames();

        Map<String, String> requestParameterMap = new HashMap<String, String>();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            String[] values = Context.getHttpRequest().getParameterValues(name.toString());
            Object value = values != null && values.length == 1 ? values[0] : values;
            if(value != null)
                requestParameterMap.put(name.toString(), value.toString());
        }
        assigns.put("request", requestParameterMap);
    }


    private static void copyRequestAttributesInto(Map assigns){
        Enumeration names = Context.getHttpRequest().getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            Object value = Context.getHttpRequest().getAttribute(name.toString());
            assigns.put(name, value);
        }
    }

    private static void copyRequestProperties(Map assigns) {
        assigns.put("request_props", map("url", Context.getHttpRequest().getRequestURL().toString()));
    }
}
