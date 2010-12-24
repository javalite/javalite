/*
Copyright 2009-2010 Igor Polevoy 

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

package javalite.http;

import java.io.OutputStream;

/**
 * Executes a POST request.
 *
 * @author Igor Polevoy
 */
public class Post extends Request<Post> {
    
    private byte[] content;

    /**
     * Constructor for making POST requests.
     *
     * @param uri URI of resource.
     * @param content content to be posted to the resource.
     * @param connectTimeout connection timeout.
     * @param readTimeout read timeout.
     */
    public Post(String uri, byte[] content, int connectTimeout, int readTimeout) {
        super(uri, connectTimeout, readTimeout);
        this.content = content;
    }

    @Override
    public Post doConnect() {
        try {
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            OutputStream out = connection.getOutputStream();
            out.write(content);
            out.flush();
            return this;
        } catch (Exception e) {
            throw new HttpException(e);
        }
    }

    public static void main(String[] args) {
        Post post = Http.post("http://localhost:8080/kitchensink/http/post", "this is a post content");
        System.out.println(post.text());
        System.out.println(post.headers());
        System.out.println(post.responseCode());
        System.out.println(post.responseMessage());
    }
}