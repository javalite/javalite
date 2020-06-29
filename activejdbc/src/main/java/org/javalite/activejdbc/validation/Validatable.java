package org.javalite.activejdbc.validation;

public interface Validatable {
    Object get(String attribute);

    /**
     * This method is not to add validators for future processing. This is instead used to add validators
     * and their respective error messages in case those validators fail validation.
     *
     * @param validator validator that failed validation (later to be used to retrieve error message)
     * @param errorKey - generally an attribute name that failed  validation
     */
    void addFailedValidator(Validator validator, String errorKey);
}
