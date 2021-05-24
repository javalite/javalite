package org.javalite.openapi;

import org.javalite.activeweb.HttpMethod;
import org.javalite.common.Util;

import java.util.ArrayList;
import java.util.List;


/**
 * Definition of an API  Endpoint
 */
public class EndPointDefinition {

    private List<HttpMethod> methods = new ArrayList<>();
    //underscore format
    private String path;
    private String openAPIdoc;
    private String argumentClassName; // can be null

    public EndPointDefinition(List<HttpMethod> methods, String path, String openAPIdoc, String argumentClassName) {

        this.methods.addAll(methods);
        this.path = path;
        this.openAPIdoc = openAPIdoc;
        this.argumentClassName = argumentClassName;
    }

    public EndPointDefinition(List<HttpMethod> methods, String path,  String argumentClassName) {
        this(methods, path, null, argumentClassName);

    }

    public List<HttpMethod> getMethods() {
        return methods;
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

    @Override
    public String toString() {
        return "method=[" + Util.join(methods, ",") + "]" +
                ", path='" + path + '\'' +
                ", openAPIdoc='" + openAPIdoc + '\'' +
                ", argumentClassName='" + argumentClassName + '\'';
    }

    public boolean hasMethod(HttpMethod method){
        return methods.stream().anyMatch(httpMethod -> httpMethod.equals(method));
    }
}
