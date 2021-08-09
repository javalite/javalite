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


import java.io.IOException;
import java.io.OutputStream;

/**
 * Executes a GET request.
 *
 * @author Igor Polevoy
 */
public class Get extends Request {

    /**
     * Constructor for making GET requests.
     *
     * @param url            URL of resource.
     * @param connectTimeout connection timeout.
     * @param readTimeout    read timeout.
     */
    public Get(String url, int connectTimeout, int readTimeout) {
        super(url, connectTimeout, readTimeout);
    }

    @Override
    protected void writeBody(OutputStream stream) throws IOException {}

    @Override
    protected String getMethod() {
        return "GET";
    }




    public static void main(String[] args) {
        Get get = Http.get("http://localhost:8080");
        System.out.println(get.text());
        System.out.println(get.headers());
        System.out.println(get.responseCode());
    }
}
