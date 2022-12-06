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

import org.javalite.activeweb.annotations.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;



/**
 * This class is mostly used by the framework. Do not use in  applications.
 *
 *
 * @author Igor Polevoy
 * @since forever.
 */
public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS;

    /**
     * Detects a method from annotation
     *
     * @param annotation class annotation
     * @return method instance corresponding to the annotation type.
     */
    public static HttpMethod method(Annotation annotation){
        if(annotation instanceof GET){
            return GET;
        }else if(annotation instanceof POST){
            return POST;
        }else if(annotation instanceof PUT){
            return PUT;
        }else if(annotation instanceof DELETE){
            return DELETE;
        }else if(annotation instanceof PATCH){
            return PATCH;
        }else if (annotation instanceof HEAD) {
            return HEAD;
        }else if (annotation instanceof OPTIONS) {
            return OPTIONS;
        }else{
            throw new IllegalArgumentException("Allowed annotations can be found in 'org.javalite.activeweb.annotations' package.");
        }
    }

    /**
     * Detects an HTTP method from a request.
     */
    static HttpMethod getMethod(HttpServletRequest request){
        String methodParam = request.getParameter("_method");
        String requestMethod = request.getMethod();
        requestMethod = requestMethod.equalsIgnoreCase("POST") && methodParam != null && methodParam.equalsIgnoreCase("DELETE")? "DELETE" : requestMethod;
        requestMethod = requestMethod.equalsIgnoreCase("POST") && methodParam != null && methodParam.equalsIgnoreCase("PUT")? "PUT" : requestMethod;
        requestMethod = requestMethod.equalsIgnoreCase("POST") && request.getHeader("X-HTTP-Method-Override") != null && request.getHeader("X-HTTP-Method-Override").equalsIgnoreCase("PATCH") ? "PATCH" : requestMethod;
        return HttpMethod.valueOf(requestMethod.toUpperCase());
    }

    public static Annotation annotation(HttpMethod httpMethod) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return switch (httpMethod) {
            case GET -> getInstance(GET.class);
            case POST -> getInstance(POST.class);
            case PUT -> getInstance(PUT.class);
            case DELETE -> getInstance(DELETE.class);
            case HEAD -> getInstance(HEAD.class);
            case  PATCH -> getInstance(PATCH.class);
            case OPTIONS -> getInstance(OPTIONS.class);
        };
    }

    private static Annotation getInstance(Class<? extends Annotation> annotation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return annotation.getDeclaredConstructor().newInstance();
    }

    /**
     * @return true if a method has an intention to change a resource.
     */
    public boolean destructive(){
        return this.equals(PUT) || this.equals(POST) || this.equals(DELETE) || this.equals(PATCH);
    }
}
