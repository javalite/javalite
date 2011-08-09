package org.javalite.activejdbc;

/**
 * @author Igor Polevoy
 */
public class StaleModelException extends RuntimeException {
    public StaleModelException(String message) {
        super(message);    
    }
}
