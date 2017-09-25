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

import java.util.Map;
import java.util.Set;

import static java.net.URLEncoder.encode;

/**
 * This is a convenience class to allow creation of request objects on one line with some pre-defined values.
 * 
 * @author Igor Polevoy
 */
public class Http {

    /**
     * Connection timeout in milliseconds. Set this value to what you like to override default.
     */
    public static final int CONNECTION_TIMEOUT = 5000;

    /**
     * Read timeout in milliseconds. Set this value to what you like to override default.
     */
    public static final int READ_TIMEOUT = 5000;
    
    private Http() {
        
    }

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
     *  Often used to post form parameters:
     *
     * <pre>
     *     Http.post("http://example.com/create").param("name1", "val1");
     * </pre>
     *
     * @param uri     url of resource.
     * @return {@link Post} object.
     */
    public static Post post(String uri) {
        return post(uri, null, CONNECTION_TIMEOUT, READ_TIMEOUT);
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
     * Executes a POST request. Often used to post form parameters:
     *
     * <pre>
     *     Http.post("http://example.com/create").param("name1", "val1");
     * </pre>
     *
     * @param url            url of resource.
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Post} object.
     */
    public static Post post(String url, int connectTimeout, int readTimeout) {

        try {
            return new Post(url, null, connectTimeout, readTimeout);
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
     * Create multipart request
     *
     * @param url URL to send to
     * @return new Multipart request
     */
    public static Multipart multipart(String url) {
        return new Multipart(url, CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Create multipart request
     *
     * @param url URL to send to
     * @param connectTimeout connect timeout
     * @param readTimeout read timeout
     * @return new Multipart request
     */
    public static Multipart multipart(String url, int connectTimeout, int readTimeout) {
        return new Multipart(url, connectTimeout, connectTimeout);
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


    /**
     * Executes a PATCH request.
     *
     * @param uri     url of resource.
     * @param content content to be posted.
     * @return {@link Patch} object.
     */
    public static Patch patch(String uri, String content) {
        return patch(uri, content.getBytes(), CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Executes a PATCH request.
     *
     * @param uri     url of resource.
     * @param content content to be posted.
     * @return {@link Patch} object.
     */
    public static Patch patch(String uri, byte[] content) {
        return patch(uri, content, CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Executes a PATCH request.
     *
     * @param url            url of resource.
     * @param content        content to be posted.
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Patch} object.
     */
    public static Patch patch(String url, byte[] content, int connectTimeout, int readTimeout) {

        try {
            return new Patch(url, content, connectTimeout, readTimeout);
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }

    /**
     * @deprecated use {@link #map2URLEncoded(Map)};
     */
    public static String map2Content(Map params){
        return map2URLEncoded(params);
    }

    /**
     * Converts a map to URL- encoded string. This is a convenience method which can be used in combination
     * with {@link #post(String, byte[])}, {@link #put(String, String)} and others. It makes it easy
     * to convert parameters to submit a string:
     *
     * <pre>
     *     key=value&key1=value1;
     * </pre>
     *
     * @param params map with keys and values to be posted. This map is used to build
     * a URL-encoded string, such that keys are names of parameters, and values are values of those
     * parameters. This method will also URL-encode keys and content using UTF-8 encoding.
     * <p>
     *     String representations of both keys and values are used.
     * </p>
     * @return URL-encided string like: <code>key=value&key1=value1;</code>
     */
    public static String map2URLEncoded(Map params){
        StringBuilder stringBuilder = new StringBuilder();
        try{
            Set keySet = params.keySet();
            Object[] keys = keySet.toArray();
            for (int i = 0; i < keys.length; i++) {
                stringBuilder.append(encode(keys[i].toString(), "UTF-8")).append("=").append(encode(params.get(keys[i]).toString(), "UTF-8"));
                if(i < (keys.length - 1)){
                    stringBuilder.append("&");
                }
            }
        }catch(Exception e){
            throw new HttpException("failed to generate content from map", e);
        }
        return stringBuilder.toString();
    }
}
