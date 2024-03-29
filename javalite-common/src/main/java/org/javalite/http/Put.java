/*
Copyright 2009-2019 Igor Polevoy

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

import java.io.OutputStream;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Map;

/**
 * Executes a PUT request.
 *
 * @author Igor Polevoy
 */
public class Put extends Post {


    /**
     * Constructor for making PUT requests.
     *
     * @param url URL of resource.
     * @param content content to be "put" into a resource.
     * @param connectTimeout connection timeout.
     * @param readTimeout read timeout.
     */
    public Put(String url, byte[] content, int connectTimeout, int readTimeout) {
        super(url, content, connectTimeout, readTimeout);
    }

    @Override
    protected String getMethod() {
        return "PUT";
    }


    public static void main(String[] args) {
        Put put = Http.put("http://localhost:8080/http", "bugagaga").param("", "");
        System.out.println(put.text());
        System.out.println(put.headers());
        System.out.println(put.responseCode());
    }
}
