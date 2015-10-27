package org.javalite.activejdbc.validation.length;

/**
 * Attribute Length validation option.
 */
public interface LengthOption {
    boolean validate(String fieldValue);

    String getParametrizedMessage();

    Object[] getMessageParameters();
}
