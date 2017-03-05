package org.javalite.http;

import java.io.OutputStream;

/**
 * Executes a PATCH request.
 *
 * @author Mikhail Chachkouski
 */
public class Patch  extends Request<Patch> {

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
    public Patch doConnect() {
        try {
            this.connection.setDoInput(true);
            this.connection.setDoOutput(true);
            this.connection.setUseCaches(false);
            this.connection.setRequestMethod("POST");
            this.connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            OutputStream out = this.connection.getOutputStream();
            out.write(this.content);
            out.flush();
            return this;
        } catch (Exception e) {
            throw new HttpException("Failed URL: " + url, e);
        }
    }
}
