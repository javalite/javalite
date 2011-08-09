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

package org.javalite.http;

/**
 * This is a convenience class to allow creation of request objects on one line with some pre-defined values.
 * 
 * @author Igor Polevoy
 */
public class Http {

    /**
     * Connection timeout in milliseconds. Set this value to what you like to override default.
     */
    public static int CONNECTION_TIMEOUT = 5000;

    /**
     * Read timeout in milliseconds. Set this value to what you like to override default.
     */
    public static int READ_TIMEOUT = 5000;




    /**
     * Executes a POST request.
     *
     * @param uri     url of resource.
     * @param content content to be posted.
     * @return {@link Post} object.
     */
    public static Post post(String uri, String content) {
        return post(uri, content.getBytes(), CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Executes a POST request.
     *
     * @param uri     url of resource.
     * @param content content to be posted.
     * @return {@link Post} object.
     */
    public static Post post(String uri, byte[] content) {
        return post(uri, content, CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Executes a POST request.
     *
     * @param url            url of resource.
     * @param content        content to be posted.
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Post} object.
     */
    public static Post post(String url, byte[] content, int connectTimeout, int readTimeout) {

        try {
            return new Post(url, content, connectTimeout, readTimeout);
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }



    /**
     * Executes a GET request.
     *
     * @param url url of the resource.
     * @return {@link Get} object.
     */
    public static Get get(String url) {
        return get(url, CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Executes a GET request
     *
     * @param url            url of resource.
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Get} object.
     */
    public static Get get(String url, int connectTimeout, int readTimeout) {

        try {
            return new Get(url, connectTimeout, readTimeout);
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }

    /**
     * Executes a PUT request.
     *
     * @param uri     url of resource.
     * @param content content to be put.
     * @return {@link Put} object.
     */
    public static Put put(String uri, String content) {
        return put(uri, content.getBytes());
    }

    /**
     * Executes a PUT request.
     *
     * @param uri     uri of resource.
     * @param content content to be put.
     * @return {@link Put} object.
     */
    public static Put put(String uri, byte[] content) {
        return put(uri, content, CONNECTION_TIMEOUT , READ_TIMEOUT);
    }

    /**
     * Executes a PUT request.
     *
     * @param url            url of resource.
     * @param content        content to be "put"
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Put} object.
     */
    public static Put put(String url, byte[] content, int connectTimeout, int readTimeout) {

        try {
            return new Put(url, content, connectTimeout, readTimeout);
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }


    /**
     * Executes a DELETE request.
     *
     * @param uri uri of resource to delete
     * @return {@link Delete}
     */
    public static Delete delete(String uri) {
        return delete(uri, CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Executes a DELETE request.
     *
     * @param url            url of resource to delete
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Delete}
     */
    public static Delete delete(String url, int connectTimeout, int readTimeout) {
        try {
            return new Delete(url, connectTimeout, readTimeout);
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }

}
