package org.javalite.activejdbc.validation;

/**
 * As the name implies, it is implicit, and used internally by ActiveWeb
 * when converting request values into Java beans.
 * <strong>Do not use  this validator directly. </strong>
 *
 * @since 3.0 and 2.3-j8
 */
public final class ImplicitConversionValidator extends ValidatorAdapter {

    public ImplicitConversionValidator(String message) {
        setMessage(message);
    }

    @Override
    public void validate(Validatable validatable) {
        //do nothing.
    }
}
