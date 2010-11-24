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

import javalite.common.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This class provides static convenience methods for simple HTTP requests.
 *
 * @author Igor Polevoy
 */
public abstract class Http<T extends Http> {

    /**
     * Connection timeout in milliseconds. Set this value to what you like to override default.
     */
    public static int CONNECTION_TIMEOUT = 5000;

    /**
     * Read timeout in milliseconds. Set this value to what you like to override default.
     */
    public static int READ_TIMEOUT = 5000;


    protected HttpURLConnection connection;
    private boolean connected;


    public Http(String uri, int connectTimeout, int readTimeout) {
        try {
            connection = (HttpURLConnection) new URL(uri).openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
        } catch (Exception e) {
            throw new HttpException(e);
        }
    }


    /**
     * Sets an HTTP header - call before making a request.
     *
     * @param name  header name
     * @param value header value.
     * @return self.
     */
    public T header(String name, String value) {
        connection.setRequestProperty(name, value);
        return (T) this;
    }


    /**
     * Returns input stream to read server response from.
     *
     * @return input stream to read server response from.
     */
    public InputStream getInputStream() {
        try {
            return connection.getInputStream();
        } catch (Exception e) {
            throw new HttpException(e);
        }
    }

    /**
     * Returns HTTP headers as sent by server.
     *
     * @return HTTP headers as sent by server.
     */
    public Map<String, List<String>> headers() {
        connect();
        return connection.getHeaderFields();
    }

    /**
     * Returns HTTP response code.
     *
     * @return HTTP response code.
     */
    public int responseCode() {
        try {
            connect();
            return connection.getResponseCode();
        } catch (Exception e) {
            throw new HttpException(e);
        }
    }

    /**
     * Returns response message from server, such as "OK", or "Created", etc.
     *
     * @return response message from server, such as "OK", or "Created", etc.
     */
    public String responseMessage() {
        try {
            connect();
            return connection.getResponseMessage();
        } catch (Exception e) {
            throw new HttpException(e);
        }
    }

    /**
     * Fetches response content from server as bytes.
     *
     * @return response content from server as bytes.
     */
    public byte[] bytes() {

        connect();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        byte[] bytes = new byte[1024];
        int count;
        try {
            InputStream in = connection.getInputStream();
            while ((count = in.read(bytes)) != -1) {
                bout.write(bytes, 0, count);
            }
        } catch (Exception e) {
            throw new HttpException(e);
        }
        return bout.toByteArray();
    }

    /**
     * Fetches response content from server as String.
     *
     * @return response content from server as String.
     */
    public String text() {
        try {
            connect();
            return Util.read(connection.getInputStream());
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    /**
     * Always call this method to clear all remaining data in connections after reading a response.
     * This will help keep-alive work smoothly.
     */
    public void dispose() {

        //according to this: http://download.oracle.com/javase/1.5.0/docs/guide/net/http-keepalive.html
        //should read all data from connection to make it happy.

        byte[] bytes = new byte[1024];
        try {
            int count = 0;
            InputStream in = connection.getInputStream();
            while ((count = in.read(bytes)) > 0) {
            }//nothing

            in.close();
        } catch (Exception ignore) {
            try {
                InputStream errorStream = connection.getErrorStream();
                int ret = 0;

                while ((ret = errorStream.read(bytes)) > 0) {
                }//nothing

                errorStream.close();
            } catch (IOException ignoreToo) {
            }
        }
    }


    protected T connect() {
        if (!connected) {
            T t = doConnect();
            connected = true;
            return t;
        } else {
            return (T) this;
        }
    }


    /**
     * Makes a connection to the remote resource.
     *
     * @return self.
     */
    protected abstract T doConnect();


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
     * @param uri            url of resource.
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Get} object.
     */
    public static Get get(String uri, int connectTimeout, int readTimeout) {

        try {
            return new Get(uri, connectTimeout, readTimeout);
        } catch (Exception e) {
            throw new HttpException(e);
        }
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
     * @param uri            url of resource.
     * @param content        content to be posted.
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Post} object.
     */
    public static Post post(String uri, byte[] content, int connectTimeout, int readTimeout) {

        try {
            return new Post(uri, content, connectTimeout, readTimeout);
        } catch (Exception e) {
            throw new HttpException(e);
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
     * @param uri            uri of resource.
     * @param content        content to be "put"
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Put} object.
     */
    public static Put put(String uri, byte[] content, int connectTimeout, int readTimeout) {

        try {
            return new Put(uri, content, connectTimeout, readTimeout);
        } catch (Exception e) {
            throw new HttpException(e);
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
     * @param uri            uri of resource to delete
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout    read timeout in milliseconds.
     * @return {@link Delete}
     */
    public static Delete delete(String uri, int connectTimeout, int readTimeout) {
        try {
            return new Delete(uri, connectTimeout, readTimeout);
        } catch (Exception e) {
            throw new HttpException(e);
        }
    }
}
