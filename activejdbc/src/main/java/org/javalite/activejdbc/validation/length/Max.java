package org.javalite.activejdbc.validation.length;

/**
 * The attribute length must be less than or equal to the given value.
 */
public class Max implements LengthOption {
    private int length;

    private Max(int length) {
        this.length = length;
    }

    public static Max of(final int length) {
        return new Max(length);
    }

    @Override
    public boolean validate(String fieldValue) {
        return fieldValue.length() <= length;
    }

    @Override
    public String getParametrizedMessage() {
        return "attribute should have a maximum length of {0}";
    }

    @Override
    public Object[] getMessageParameters() {
        return new Object[]{length};
    }
}