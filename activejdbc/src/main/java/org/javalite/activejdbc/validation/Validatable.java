package org.javalite.activejdbc.validation;

import java.util.Locale;


/**
 * Top interface to make something validatable.
 * Top interface to make something validatable.
 */
public interface Validatable {

    /**
     * Used by validators to get values
     *
     * @param attribute name of attribute
     * @return value of attribute
     */
    Object get(String attribute);

    /**
     * This method is not to add validators for future processing. This is instead used to add validators
     * and their respective error messages in case those validators fail.
     *
     * @param validator validator that failed validation (later to be used to retrieve error message)
     * @param errorKey - generally an attribute name that failed  validation
     */
    void addFailedValidator(Validator validator, String errorKey);

    /**
     * Implementation should call {#link validate()} internally.
     *
     * @return true if  object is valid.
     */
    boolean isValid();

    /**
     * Runs  validation. Will blow away any  previous validation errors.
     */
    void validate();

    /**
     * Runs  validation.
     *
     * @param reset true to reset all previous validation errors.
     */
    void validate(boolean reset);


    /**
     * Provides an instance of localized <code>Errors</code> object, filled with error messages after validation.
     *
     * @return an instance of localized <code>Errors</code> object, filled with error messages after validation.
     */
    Errors errors();

    /**
     * Provides an instance of localized <code>Errors</code> object, filled with error messages after validation.
     *
     * @param locale locale to pick the right resource bundle.
     * @return an instance of localized <code>Errors</code> object, filled with error messages after validation.
     */
    Errors errors(Locale locale);
}
