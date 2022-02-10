package org.javalite.activeweb;

import org.javalite.json.JSONParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.join;


/**
 * Wraps multiple HTTP methods for a single endpoint.  For instance:
 * http://university.com/api/students may have two methods: GET and POST, one to get students, another to save,
 * each with different semantics and documentation.
 */
public class EndPointDefinition {

    private final List<EndPointHttpMethod> endpointMethods = new ArrayList<>();
    private final String path;
    private final String controllerClassName;
    private final String actionMethodName;
    private final String argumentClassName; // can be null


    public EndPointDefinition(List<EndPointHttpMethod> endpointMethods, String path, String controllerClassName,
                              String actionMethod, String argumentClassName) {

        this.endpointMethods.addAll(endpointMethods);
        this.path = path;
        this.argumentClassName = argumentClassName;
        this.controllerClassName = controllerClassName;
        this.actionMethodName = actionMethod;
    }


    public String getActionMethodName() {
        return actionMethodName;
    }

    public String getDisplayControllerMethod() {
        return blank(argumentClassName) ? actionMethodName + "()" : actionMethodName + "(" + argumentClassName + ")";

    }

    public List<EndPointHttpMethod> getEndpointMethods() {
        return endpointMethods;
    }

    public List<HttpMethod> getHTTPMethods() {
        return this.endpointMethods.stream().map(EndPointHttpMethod::getHttpMethod).collect(Collectors.toList());
    }

    public String getPath() {
        return path;
    }

    public String getArgumentClassName() {
        return argumentClassName;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    public boolean hasOpenAPI() {
        boolean hasAPI = false;
        for (EndPointHttpMethod endpointHttpMethod : endpointMethods) {
            if(!blank(endpointHttpMethod.getHttpMethodAPI())){
                hasAPI = true;
            }
        }
        return hasAPI;
    }

    public boolean contains(EndPointHttpMethod[] endpointHttpMethods) {
        for (EndPointHttpMethod endpointHttpMethod : endpointHttpMethods) {
             if(!this.endpointMethods.contains(endpointHttpMethod)){
                 return false;
             }
        }
        return true;
    }


    /**
     *
     * @return a map that looks like:
     * <pre><code>
     * {
     *  "post": { "summary" : "Add new pet" ....},
     *  "put":  { "summary" : "Update existing pet" ....}
     *  }
     *  </code>
     *  </pre>
     */
    Map<String, Map<String, Object>> getEndpointAPI(){

        Map<String, Map<String, Object>> apiMethods =   new HashMap<>();
        for (EndPointHttpMethod endPointHttpMethod : endpointMethods) {
            try{
                apiMethods.put(endPointHttpMethod.getHttpMethod().name().toLowerCase(), endPointHttpMethod.getAPIAsMap());
            }catch(JSONParseException e){
                throw new JSONParseException("Failed to parse docs into JSON for: " + this.getControllerClassName() + ", path: " + this.path + ":" +  endPointHttpMethod.getHttpMethod().name(), e);
            }
        }
        return apiMethods;
    }

    public void addEndpointMethod(List<EndPointHttpMethod> endpointMethod) {

    }
}


