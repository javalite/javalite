package org.javalite.activeweb.proxy;

public class ProxyWriterException extends RuntimeException {

    public ProxyWriterException() {
        super("Failed to write to underlying stream");
    }

    public ProxyWriterException(Throwable cause) {
        super("Failed to write to underlying stream", cause);
    }
}
