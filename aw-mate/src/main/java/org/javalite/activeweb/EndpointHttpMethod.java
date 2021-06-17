package org.javalite.activeweb;

/**
 * This record is to encapsulate an HTTP method and its corresponding API docs if any.
 */
public class EndpointHttpMethod{
    private HttpMethod httpMethod;
    private String httpMethodAPI;

    public EndpointHttpMethod(HttpMethod httpMethod, String httpMethodAPI) {
        this.httpMethod = httpMethod;
        this.httpMethodAPI = httpMethodAPI;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getHttpMethodAPI() {
        return httpMethodAPI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndpointHttpMethod that = (EndpointHttpMethod) o;

        if (httpMethod != that.httpMethod) return false;
        return httpMethodAPI != null ? httpMethodAPI.equals(that.httpMethodAPI) : that.httpMethodAPI == null;
    }

}