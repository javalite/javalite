package org.javalite.activejdbc.validation.length.option;

/**
 * Attribute Length validation option.
 */
public interface LengthOption {
    boolean validate(String fieldValue);

    String getParameterizedMessage();

    Object[] getMessageParameters();
}
