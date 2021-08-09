package org.javalite.http;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMethod<T extends Request> extends Request {

    private byte[] content;
    private Map<String, String> params = new HashMap<>();

    public AbstractMethod(String url, byte[] content, int connectTimeout, int readTimeout) {
        super(url, connectTimeout, readTimeout);
        this.content = content;
    }

    public AbstractMethod(String url, int connectTimeout, int readTimeout) {
        super(url, connectTimeout, readTimeout);
    }

    protected abstract String getMethod();


    @Override
    public  T doConnect() {
        try {
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod(getMethod());
            connection.setInstanceFollowRedirects(redirect);

            if(params.size() > 0){
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            OutputStream out = connection.getOutputStream();
            if(params.size() > 0){
                out.write(Http.map2URLEncoded(params).getBytes());
            }
            if(content != null){
                out.write(content);
            }

            out.flush();
            return (T) this;
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }



}
