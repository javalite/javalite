package org.javalite.app_config;

public class ConfigInitException extends RuntimeException {
    public ConfigInitException(Throwable cause) {
        super(cause);
    }

    public ConfigInitException(String message) {
        super(message);
    }
}
