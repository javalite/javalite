package org.javalite.activeweb;

/**
 * Thrown in cases when a controller has multiple overloaded action methods.
 * In such cases a framework cannot make a decision as to which  method to a route request to.
 */
public class AmbiguousActionException extends RuntimeException {
    public AmbiguousActionException(String message) {
        super(message);
    }
}
