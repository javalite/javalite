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

package org.javalite.http;

import org.javalite.common.Collections;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.map;

/**
 * Executes a POST request.
 *
 * @author Igor Polevoy
 */
public class Post extends Request<Post> {

    private final byte[] content;
    private Map<String, String> params = new HashMap<>();

    /**
     * Constructor for making POST requests.
     *
     * @param url URL of resource.
     * @param content content to be posted to the resource.
     * @param connectTimeout connection timeout.
     * @param readTimeout read timeout.
     */
    public Post(String url, byte[] content, int connectTimeout, int readTimeout) {
        super(url, connectTimeout, readTimeout);
        this.content = content;
    }

    @Override
    public Post doConnect() {
        try {
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(redirect);

            if(params.size() > 0){
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            OutputStream out = connection.getOutputStream();
            if(params.size() > 0){
                out.write(Http.map2Content(params).getBytes());
            }
            if(content != null){
                out.write(content);
            }

            out.flush();
            return this;
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }


    /**
     * Convenience method to add multiple parameters to the request.
     * <p></p>
     * Names and values alternate: name1, value1, name2, value2, etc.
     *
     * @param namesAndValues names/values of multiple fields to be added to the request.
     * @return self
     */
    public Post params(String ... namesAndValues){

        if(namesAndValues == null ){
            throw new NullPointerException("'names and values' cannot be null");
        }

        if(namesAndValues.length % 2 != 0){
            throw new IllegalArgumentException("mus pas even number of arguments");
        }

        for (int i = 0; i < namesAndValues.length - 1; i += 2) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("parameter names cannot be nulls");
            params.put(namesAndValues[i], namesAndValues[i + 1]);
        }
        return this;
    }

    /**
     * Adds a parameter to the request as in a HTML form.
     *
     * @param name name of parameter
     * @param value value of parameter
     * @return self
     */
    public Post param(String name, String value){
        params.put(name, value);
        return this;
    }

    public static void main(String[] args) {
//        Post post = Http.post("http://localhost:8080/kitchensink/http/post", "this is a post content").header("Content-type", "text/json");
//        //System.out.println(post.text());
//        //System.out.println(post.headers());
//        System.out.println(post.responseCode());
//        System.out.println(post.responseMessage());

        Post post = Http.post("http://localhost:8080/hello").param("name", "John");
        System.out.println(post.text());
    }
}