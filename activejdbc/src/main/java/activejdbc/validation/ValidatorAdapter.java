package activejdbc.validation;

import activejdbc.Messages;
import activejdbc.Model;

import java.util.Locale;

/**
 * Subclass this class to create custom validators.
 *
 * @author Igor Polevoy
 */
public abstract class ValidatorAdapter implements Validator{
    protected String message;

    public abstract void validate(Model m);

    public final void setMessage(String message) {
        this.message = message;
    }

    public String formatMessage(Locale locale, Object ... params) {
        return locale != null ? Messages.message(message, locale, params) : Messages.message(message, params);     
    }
}