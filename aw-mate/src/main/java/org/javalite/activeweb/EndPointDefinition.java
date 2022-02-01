package org.javalite.activeweb;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import static org.javalite.common.Collections.list;
import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.join;


/**
 * Wraps multiple HTTP methods for a single end point. Generally an edge case, but we need it. For instance:
 * http://university.com/api/students may have two methods: GET and POST, one to get students, another to save,
 * each with different semantics and documentation.
 */
public class EndPointDefinition {

    private final List<EndpointHttpMethod> endpointMethods = new ArrayList<>();
    private final String path;
    private final String controllerClassName;
    private final String actionMethodName;
    private final String argumentClassName; // can be null


    public EndPointDefinition(List<EndpointHttpMethod> endpointMethods, String path, String controllerClassName,
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

    public List<EndpointHttpMethod> getEndpointMethods() {
        return endpointMethods;
    }

    public List<HttpMethod> getHTTPMethods() {
        return this.endpointMethods.stream().map(EndpointHttpMethod::getHttpMethod).collect(Collectors.toList());
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
        for (EndpointHttpMethod endpointHttpMethod : endpointMethods) {
            if(!blank(endpointHttpMethod.getHttpMethodAPI())){
                hasAPI = true;
            }
        }
        return hasAPI;
    }

    public boolean contains(EndpointHttpMethod[] endpointHttpMethods) {
        for (EndpointHttpMethod endpointHttpMethod : endpointHttpMethods) {
             if(!this.endpointMethods.contains(endpointHttpMethod)){
                 return false;
             }
        }
        return true;
    }
}


