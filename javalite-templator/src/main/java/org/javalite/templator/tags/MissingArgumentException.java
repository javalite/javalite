package org.javalite.templator.tags;

/**
 * @author Igor Polevoy on 1/11/15.
 */
public class MissingArgumentException extends RuntimeException {
    public MissingArgumentException(String message) {
        super(message);
    }
}
