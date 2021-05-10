package org.javalite.openapi;

import org.javalite.activeweb.HttpMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Definition of an API  Endpoint
 */
public class EndPointDefinition {

    private HttpMethod method;
    //underscore format
    private String path;
    private String openAPIdoc;
    private String argumentClassName; // can be null

    public EndPointDefinition(HttpMethod method, String path, String openAPIdoc, String argumentClassName) {
        this.method = method;
        this.path = path;
        this.openAPIdoc = openAPIdoc;
        this.argumentClassName = argumentClassName;
    }

    public EndPointDefinition(HttpMethod method, String path,  String argumentClassName) {
        this(method, path, null, argumentClassName);

    }

    public HttpMethod getMethod() {
        return method;
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
        return "method=" + method +
                ", path='" + path + '\'' +
                ", openAPIdoc='" + openAPIdoc + '\'' +
                ", argumentClassName='" + argumentClassName + '\'' +
                '}';
    }

    String[] toArray(){
        return new String[]{method.name(), path, argumentClassName == null ? "" : argumentClassName};
    }
}
