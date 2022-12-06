package org.javalite.activeweb;

import org.javalite.json.JSONHelper;
import org.javalite.json.JSONMap;
import org.javalite.json.JSONParseException;

/**
 * This class is to encapsulate an HTTP method (GET, POST, DELETE, etc.) and its corresponding API docs if any.
 */
public class EndPointHttpMethod {
    private final HttpMethod httpMethod;
    private final String httpMethodAPI;

    public EndPointHttpMethod(HttpMethod httpMethod, String httpMethodAPI) {
        this.httpMethod = httpMethod;
        this.httpMethodAPI = httpMethodAPI;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getHttpMethodAPI() {
        return httpMethodAPI;
    }

    public JSONMap getAPIAsMap(){
        try{
            return httpMethodAPI != null ? JSONHelper.toJSONMap(httpMethodAPI) : new JSONMap();
        }catch(JSONParseException e){
             throw new JSONParseException("Failed to parse this into JSON: " + httpMethodAPI);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndPointHttpMethod that = (EndPointHttpMethod) o;

        if (httpMethod != that.httpMethod) return false;
        return httpMethodAPI != null ? httpMethodAPI.equals(that.httpMethodAPI) : that.httpMethodAPI == null;
    }

}