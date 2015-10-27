package org.javalite.activejdbc.validation.length;

/**
 * The attribute length must be more than or equal to the given value.
 */
public class Min implements LengthOption {
    private int length;

    private Min(int length) {
        this.length = length;
    }

    public static Min of(final int length) {
        return new Min(length);
    }

    @Override
    public boolean validate(String fieldValue) {
        return fieldValue.length() >= length;
    }

    @Override
    public String getParametrizedMessage() {
        return "attribute should have a minimum length of {0}";
    }

    @Override
    public Object[] getMessageParameters() {
        return new Object[]{length};
    }
}