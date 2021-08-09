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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Util.read;
import static org.javalite.common.Util.toBase64;

/**
 * This class provides static convenience methods for simple HTTP requests.
 *
 * @author Igor Polevoy
 */
public abstract class Request {

    protected final HttpURLConnection connection;
    private boolean connected;
    protected boolean redirect;
    protected final String url;
    private Map<String, String> params = new HashMap<>();


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
    public <T extends Request> T  header(String name, String value) {
        connection.setRequestProperty(name, value);
        return (T) this;
    }

    /**
     * Configures this request to follow redirects. Default is <code>false</code>.
     *
     * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html#instanceFollowRedirects">HttpURLConnection.html#instanceFollowRedirects</a>
     * @param redirect true to follow, false to not.
     * @return self
     */
    public <T extends Request> T   redirect(boolean redirect) {
        this.redirect = redirect;
        return (T)this;
    }

    /**
     * Returns input stream to read server response from.
     *
     * @return input stream to read server response from.
     */
    public InputStream getInputStream() {
        try {
            return connection.getInputStream();
        }catch(SocketTimeoutException e){
            throw new HttpException("Failed URL: " + url +
                    ", waited for: " + connection.getConnectTimeout() + " milliseconds", e);
        }catch (Exception e) {
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
    public byte[]  bytes() {

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
        }finally {
            dispose();
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
            return responseCode() >= 400 ? read(connection.getErrorStream()) : read(connection.getInputStream());
        } catch (IOException e) {
            throw new HttpException("Failed URL: " + url, e);
        }finally {
            dispose();
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
            return responseCode() >= 400 ? read(connection.getErrorStream()) : read(connection.getInputStream(), encoding);
        } catch (IOException e) {
            throw new HttpException("Failed URL: " + url, e);
        }finally {
            dispose();
        }
    }


    /**
     * This method is already called from {@link #text()} and {@link #bytes()}, you do not have to call it if you use
     * those methods.
     * <p></p>
     * However, if you use {@link #getInputStream()}, call this method in those cases when you think you did
     * not read entire content from the stream.
     *
     * <p></p>
     * This method clears all remaining data in connections after reading a response.
     * This will help keep-alive work smoothly.
     */
    public void dispose() {
        //according to this: http://download.oracle.com/javase/1.5.0/docs/guide/net/http-keepalive.html
        //should read all data from connection to make it happy.
        byte[] bytes = new byte[1024];
        try (InputStream in = connection.getInputStream()){
            if(in != null){
                while ((in.read(bytes)) > 0) {}//do nothing
            }
        } catch (Exception ignore) {
            try(InputStream errorStream = connection.getErrorStream()) {
                if(errorStream != null){
                    while ((errorStream.read(bytes)) > 0) {}//do nothing
                }
            } catch (IOException ignoreToo) {}
        }
    }

    protected <T extends Request> T   connect() {
        if (!connected) {
            T t = doConnect();
            connected = true;
            return t;
        } else {
            return (T) this;
        }
    }




    /**
     * Sets a user and password for basic authentication.
     *
     * @param user user.
     * @param password password.
     * @return self.
     */
    public <T extends Request> T   basic(String user, String password){
        connection.setRequestProperty("Authorization", "Basic " + toBase64((user + ":" + password).getBytes()));
        return (T)this;
    }

    /**
     * Set a user authentication
     *
     * @param user user
     * @return self
     */
    public <T extends Request> T  basic(String user){
        connection.setRequestProperty("Authorization", "Basic " + toBase64((user).getBytes()));
        return (T)this;
    }

    protected <T extends Request> T  doConnect() {
        try {
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(redirect);
            connection.setRequestMethod(getMethod());
            if(!this.getClass().equals(Get.class)){
                OutputStream out = connection.getOutputStream();
                writeBody(out);
                out.flush();
                out.close();
            }
            return (T) this;
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }

    protected abstract void writeBody(OutputStream stream) throws IOException;


    protected abstract String getMethod();

    protected Map<String, String> params() {
        return params;
    }

    /**
     * Convenience method to add multiple parameters to the request.
     * <p></p>
     * Names and values alternate: name1, value1, name2, value2, etc.
     *
     * @param namesAndValues names/values of multiple fields to be added to the request.
     * @return self
     */
    public <T extends Request> T params(String ... namesAndValues) {

        if(namesAndValues == null ){
            throw new NullPointerException("'names and values' cannot be null");
        }

        if(namesAndValues.length % 2 != 0){
            throw new IllegalArgumentException("must pass even number of arguments");
        }

        for (int i = 0; i < namesAndValues.length - 1; i += 2) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("parameter names cannot be nulls");
            params.put(namesAndValues[i], namesAndValues[i + 1]);
        }

        return (T)this;
    }

    /**
     * Adds a parameter to the request as in a HTML form.
     *
     * @param name name of parameter
     * @param value value of parameter
     * @return self
     */
    public <T extends Request> T  param(String name, String value) {
        params.put(name, value);
        return (T)this;
    }
}
