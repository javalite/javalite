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
 * Executes a POST request.
 *
 * @author Igor Polevoy
 */
public class Post extends Request {

    private byte[] content;

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
        header("Content-type", "application/x-www-form-urlencoded");
    }

    @Override
    protected void writeBody(OutputStream stream) throws IOException {
        if(params().size() > 0){
            stream.write(Http.map2URLEncoded(params()).getBytes());
        }
        if (content != null) {
            stream.write(content);
        }
    }

    @Override
    protected String getMethod() {
        return "POST";
    }

    public static void main(String[] args) {


        Post post = Http.post("http://localhost:8080/http/post").
                param("greeting", "hello").header("Content-type", "text/json");

        System.out.println("Text: " + post.text());
        System.out.println("Headers: " + post.headers());
        System.out.println("Response code: " + post.responseCode());
        System.out.println("Response message: " + post.responseMessage());

    }
}