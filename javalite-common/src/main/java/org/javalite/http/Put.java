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

import java.io.OutputStream;

/**
 * Executes a PUT request.
 *
 * @author Igor Polevoy
 */
public class Put extends Request<Put> {

    private final byte[] content;

    /**
     * Constructor for making PUT requests.
     *
     * @param url URL of resource.
     * @param content content to be "put" into a resource.
     * @param connectTimeout connection timeout.
     * @param readTimeout read timeout.
     */
    public Put(String url, byte[] content, int connectTimeout, int readTimeout) {
        super(url, connectTimeout, readTimeout);
        this.content = content;
    }
    
    @Override
    public Put doConnect() {
        try {
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("PUT");
            connection.setInstanceFollowRedirects(redirect);
            OutputStream os = connection.getOutputStream();
            os.write(content);
            os.flush();
            return this;
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }

    public static void main(String[] args) {
        Put put = Http.put("http://localhost:8080/kitchensink/http/put", "bugagaga");
        System.out.println(put.text());
        System.out.println(put.headers());
        System.out.println(put.responseCode());
    }
}
