package org.javalite.templator;

/**
 * @author Igor Polevoy on 1/11/15.
 */
public class TemplateException extends RuntimeException {
    public TemplateException(Throwable cause) {
        super(cause);
    }

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
