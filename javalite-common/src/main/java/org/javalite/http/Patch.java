package org.javalite.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * Executes a PATCH request.
 *
 * @author Mikhail Chachkouski
 */
public class Patch  extends Request {

    private final byte[] content;

    /**
     * Constructor for making PATCH requests.
     *
     * @param uri URI of resource.
     * @param content content to be posted to the resource.
     * @param connectTimeout connection timeout.
     * @param readTimeout read timeout.
     */
    public Patch(String uri, byte[] content, int connectTimeout, int readTimeout) {
        super(uri, connectTimeout, readTimeout);
        this.content = content;
    }

    @Override
    public Patch doConnect(HttpURLConnection connection) throws IOException {
        connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        OutputStream out = connection.getOutputStream();
        out.write(this.content);
        out.flush();
        return this;
    }

    @Override
    protected String getMethod() {
        return "POST";
    }
}
