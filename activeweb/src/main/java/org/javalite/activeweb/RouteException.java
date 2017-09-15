package org.javalite.activeweb;

/**
 * @author igor on 7/11/14.
 */
public class RouteException extends WebException {
    public RouteException(String message) {
        super(message);
    }

    public RouteException(String message, Throwable cause) {
        super(message, cause);
    }
}
