package org.javalite.activeweb;

public class OpenAPIException extends RuntimeException{
    public OpenAPIException(Throwable cause) {
        super(cause);
    }

    public OpenAPIException(String message) {
        super(message);
    }

    public OpenAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
