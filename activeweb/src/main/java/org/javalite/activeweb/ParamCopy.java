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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
@SuppressWarnings("unchecked")
class ParamCopy {

    private ParamCopy() {}

    static void copyInto(Map assigns){
        insertActiveWebParamsInto(assigns);
        copyRequestAttributesInto(assigns);
        copyRequestParamsInto(assigns);
        copySessionAttrsInto(assigns);
        copyRequestProperties(assigns);
    }

    private static void insertActiveWebParamsInto(Map assigns) {
        assigns.put("context_path", RequestContext.getHttpRequest().getContextPath());
        //in some cases the Route is missing - for example, when exception happened before Router was invoked.
        Route route = RequestContext.getRoute();

        Map params = map("environment", Configuration.getEnv());

        if(route != null){
            params.put("controller", RequestContext.getRoute().getControllerPath());
            params.put("action", RequestContext.getRoute().getActionName());
            params.put("restful", RequestContext.getRoute().getController().restful());
        }
        assigns.put("activeweb", params);
    }


    private static void copySessionAttrsInto(Map assigns) {

        Map<String, Object> sessionAttrs = SessionHelper.getSessionAttributes();
        if (sessionAttrs.containsKey("flasher")){ //flasher is special
            assigns.put("flasher", sessionAttrs.get("flasher"));
        }
        assigns.put("session", sessionAttrs);
    }


    private static void copyRequestParamsInto(Map assigns) {
        Enumeration names = RequestContext.getHttpRequest().getParameterNames();

        Map<String, String> requestParameterMap = new HashMap<>();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            String[] values = RequestContext.getHttpRequest().getParameterValues(name.toString());
            Object value = values != null && values.length == 1 ? values[0] : values;
            if(value != null)
                requestParameterMap.put(name.toString(), value.toString());
        }
        assigns.put("request", requestParameterMap);
    }


    private static void copyRequestAttributesInto(Map assigns){
        Enumeration names = RequestContext.getHttpRequest().getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            Object value = RequestContext.getHttpRequest().getAttribute(name.toString());
            assigns.put(name, value);
        }
    }

    private static void copyRequestProperties(Map assigns) {
        assigns.put("request_props", map("url", RequestContext.getHttpRequest().getRequestURL().toString()));
    }
}
