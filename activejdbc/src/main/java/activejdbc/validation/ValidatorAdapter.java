package activejdbc.validation;

import activejdbc.Model;

/**
 * Subclass this class to create custom validators.
 *
 * @author Igor Polevoy
 */
public abstract class ValidatorAdapter implements Validator{
    private String message;

    public abstract void validate(Model m);

    public final void setMessage(String message) {
        this.message = message;
    }
}