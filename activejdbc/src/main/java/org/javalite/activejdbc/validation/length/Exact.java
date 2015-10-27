package org.javalite.activejdbc.validation.length;

/**
 * The attribute length must be equal to a given value.
 */
public class Exact implements LengthOption {
    private int length;

    private Exact(int length) {
        this.length = length;
    }

    public static Exact of(int length) {
        return new Exact(length);
    }

    @Override
    public boolean validate(String fieldValue) {
        return fieldValue.length() == length;
    }

    @Override
    public String getParametrizedMessage() {
        return "attribute should have an exact length of {0}";
    }

    @Override
    public Object[] getMessageParameters() {
        return new Object[]{length};
    }
}
