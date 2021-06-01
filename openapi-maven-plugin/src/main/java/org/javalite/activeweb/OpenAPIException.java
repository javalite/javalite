package org.javalite.activeweb;

public class OpenAPIException extends RuntimeException{
    public OpenAPIException(Throwable cause) {
        super(cause);
    }

    public OpenAPIException(String message) {
        super(message);
    }
}
