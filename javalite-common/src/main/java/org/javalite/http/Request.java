/*
Copyright 2009-2014 Igor Polevoy

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Util.read;

/**
 * This class provides static convenience methods for simple HTTP requests.
 *
 * @author Igor Polevoy
 */
public abstract class Request<T extends Request> {

    protected HttpURLConnection connection;
    private boolean connected;
    protected String url;



    public Request(String url, int connectTimeout, int readTimeout) {
        try {
            this.url = url;
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
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
            throw new HttpException("Failed URL: " + url, e);
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
            throw new HttpException("Failed URL: " + url, e);
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
            throw new HttpException("Failed URL: " + url, e);
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
            throw new HttpException("Failed URL: " + url, e);
        }
        dispose();
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
            String result = responseCode() >= 400 ? read(connection.getErrorStream()) : read(connection.getInputStream());
            dispose();
            return result;
        } catch (IOException e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }

    /**
     * Fetches response content from server as String.
     *
     * @param encoding - name of supported charset to apply when reading data.
     *
     * @return response content from server as String.
     */
    public String text(String encoding) {
        try {
            connect();
            String result = responseCode() >= 400 ? read(connection.getErrorStream()) : read(connection.getInputStream(), encoding);
            dispose();
            return result;
        } catch (IOException e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }


    /**
     * This method is already called from {@link #text()} and {@link #bytes()}, you do not have to call it if you use
     * those methods.
     * <p/> 
     * However, if you use {@link #getInputStream()}, call this method in those cases when you think you did
     * not read entire content from the stream.
     *
     * <p/>
     * This method clears all remaining data in connections after reading a response.
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
     * Sets a user and password for basic authentication.
     *
     * @param user user.
     * @param password password.
     * @return self.
     */
    public T basic(String user, String password){
        Authenticator.setDefault(new BasicAuthenticator(user, password));
        return (T) this;
    }

    class BasicAuthenticator extends Authenticator {
        private String user, password;
        BasicAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }        
        public PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication(user, password.toCharArray()));
            }
        }
}
