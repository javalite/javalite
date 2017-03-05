package org.javalite.activejdbc.validation.length;

/**
 * The attribute length must be between the given minimum and maximum length (inclusive).
 */
public class Range implements LengthOption {
    private int min;
    private int max;

    private Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static Range of(int min, int max) {
        return new Range(min, max);
    }

    @Override
    public boolean validate(String fieldValue) {
        int fieldValueLength = fieldValue.length();
        return fieldValueLength >= min && fieldValueLength <= max;
    }

    @Override
    public String getParametrizedMessage() {
        return "attribute should have a length between {0} and {1} (inclusive)";
    }

    @Override
    public Object[] getMessageParameters() {
        return new Object[]{min, max};
    }
}
