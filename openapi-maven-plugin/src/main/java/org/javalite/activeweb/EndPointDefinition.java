package org.javalite.activeweb;

import java.util.ArrayList;
import java.util.List;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Util.blank;


/**
 * Definition of an API  Endpoint
 */
public class EndPointDefinition {

    private List<HttpMethod> httpMethods = new ArrayList<>();
    //underscore format
    private final String path;
    private String openAPIdoc;
    private String controllerClassName;
    private String actionMethodName;
    private final String argumentClassName; // can be null


    public EndPointDefinition(List<HttpMethod> httpMethods, String path, String controllerClassName,
                              String controllerMethod, String argumentClassName, String openAPIdoc) {

        this.httpMethods.addAll(httpMethods);
        this.path = path;
        this.argumentClassName = argumentClassName;
        this.controllerClassName = controllerClassName;
        this.actionMethodName = controllerMethod;
        this.openAPIdoc = openAPIdoc;
    }

    //used in tests
    public EndPointDefinition(HttpMethod httpMethod, String path, String controllerClassName,
                              String controllerMethod, String argumentClassName, String openAPIdoc) {
        this(list(httpMethod), path, controllerClassName, controllerMethod,argumentClassName, openAPIdoc);
    }

    public String getActionMethodName() {
        return actionMethodName;
    }

    public String getDisplayControllerMethod() {
        return blank(argumentClassName) ? actionMethodName + "()" : actionMethodName + "(" + argumentClassName + ")";

    }

    public List<HttpMethod> getHTTPMethods() {
        return httpMethods;
    }

    public String getPath() {
        return path;
    }

    public String getOpenAPIdoc() {
        return openAPIdoc;
    }

    public String getArgumentClassName() {
        return argumentClassName;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    @Override
    public String toString() {
        return "methods=" + httpMethods +
                ", path='" + path + "'" +
                ", openAPIdoc='" + openAPIdoc + "'" +
                ", controllerClassName='" + controllerClassName + "'" +
                ", actionMethod='" + actionMethodName + "'" +
                ", openAPIdoc='" + openAPIdoc + "'" +
                ", argumentClassName='" + this.argumentClassName+ "'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndPointDefinition that = (EndPointDefinition) o;

        if (!sameMethods(that.httpMethods)) return false;
        if (!path.equals(that.path)) return false;
        if (!openAPIdoc.equals(that.openAPIdoc)) return false;
        if (!controllerClassName.equals(that.controllerClassName)) return false;
        if (!actionMethodName.equals(that.actionMethodName)) return false;
        return argumentClassName.equals(that.argumentClassName);
    }

    private boolean sameMethods(List<HttpMethod> httpMethods){
        for (HttpMethod httpMethod : this.httpMethods) {
            if(!httpMethods.contains(httpMethod)){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = httpMethods.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + openAPIdoc.hashCode();
        result = 31 * result + controllerClassName.hashCode();
        result = 31 * result + actionMethodName.hashCode();
        result = 31 * result + argumentClassName.hashCode();
        return result;
    }

    public boolean hasMethod(HttpMethod method) {
        return httpMethods.stream().anyMatch(httpMethod -> httpMethod.equals(method));
    }

    public boolean hasOpenAPI() {
            return !blank(this.openAPIdoc);
    }
}


