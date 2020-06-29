package org.javalite.activejdbc.validation;


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Validation extends ValidationSupport implements Validatable {

    private Map<String, Validator> failedValidatorMap = new HashMap<>();

    @Override
    public Object get(String attributeName) {
        try {
            boolean needRevert = false;
            Field field = getClass().getDeclaredField(attributeName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
                needRevert = true;
            }

            Object value = field.get(this);

            if (needRevert) {
                field.setAccessible(false);
            }
            return value;
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }
    @Override
    public void addFailedValidator(Validator validator, String errorKey) {
        failedValidatorMap.put(errorKey, validator);
    }

    public boolean valid() {
        validate();
        return failedValidatorMap.size() == 0;
    }

    private void validate() {
        failedValidatorMap = new HashMap<>();
        for (Validator validator : validators()) {
            validator.validate(this);
        }
    }

}
