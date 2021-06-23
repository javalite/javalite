package org.javalite.validation;

import org.javalite.common.CaseInsensitiveMap;
import org.javalite.common.Util;
import org.javalite.conversion.Converter;
import org.javalite.conversion.DateToStringConverter;
import org.javalite.conversion.StringToSqlDateConverter;
import org.javalite.conversion.StringToTimestampConverter;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Manages validators and converters.
 */
public class ValidationSupport implements Validatable {

    private final Map<String, List<Converter>> attributeConverters = new CaseInsensitiveMap<>();
    private final List<Validator> validators = new ArrayList<>();
    private Errors errors = new Errors();
    /**
     * Registers converter for specified model attribute.
     */
    public void convertWith(Converter converter, String attribute) {
        attributeConverters.computeIfAbsent(attribute, k -> new ArrayList<>()).add(converter);
    }

    /**
     * Returns converter for specified model attribute, able to convert from sourceClass to destinationClass.
     * Returns null if no suitable converter was found.
     */
    public  <S, T> Converter<S, T> converterForClass(String attribute, Class<S> sourceClass, Class<T> destinationClass) {
        List<Converter> list = attributeConverters.get(attribute);
        if (list != null) {
            for (Converter converter : list) {
                if (converter.canConvert(sourceClass, destinationClass)) {
                    return converter;
                }
            }
        }
        return null;
    }

    /**
     * Returns converter for specified model attribute, able to convert value to an instance of destinationClass.
     * Returns null if no suitable converter was found.
     */
    @SuppressWarnings("unchecked")
    public <T> Converter<Object, T> converterForValue(String attribute, Object value, Class<T> destinationClass) {
        return converterForClass(attribute,
                value != null ? (Class<Object>) value.getClass() : Object.class, destinationClass);
    }

    @SuppressWarnings("unchecked")
    public ValidationBuilder validateWith(Validator validator) {
        validators.add(validator);
        return new ValidationBuilder(validator);
    }

    @SuppressWarnings("unchecked")
    public ValidationBuilder validateWith(List<Validator> list) {
        this.validators.addAll(list);
        return new ValidationBuilder(list);
    }

    public NumericValidationBuilder validateNumericalityOf(String... attributes) {
        List<NumericValidator> list = new ArrayList<>();
        for (String attribute : attributes) {
            NumericValidator validator = new NumericValidator(attribute);
            list.add(validator);
            validators.add(validator);
        }
        return new NumericValidationBuilder(list);
    }

    public ValidationBuilder validatePresenceOf(String... attributes) {
        List<Validator> list = new ArrayList<>();
        for (String attribute : attributes) {
            list.add(new AttributePresenceValidator(attribute));
        }
        return validateWith(list);
    }

    public void removeValidator(Validator validator) {
        validators.remove(validator);
    }

    //TODO: this is returning a mutable list, not  good!
    public List<Validator> validators() {
        return validators;
    }


    /**
     * Registers date converters (Date -> String -> java.sql.Date) for specified model attributes.
     */
    public void dateFormat(String pattern, String... attributes) {
        dateFormat(new SimpleDateFormat(pattern), attributes);
    }

    /**
     * Registers date converters (Date -> String -> java.sql.Date) for specified model attributes.
     */
    public void dateFormat(DateFormat format, String... attributes) {
        convertWith(new DateToStringConverter(format), attributes);
        convertWith(new StringToSqlDateConverter(format), attributes);
    }

    /**
     * Registers timestamp converters (Date -> String -> java.sql.Timestamp) for specified model attributes.
     */
    public void timestampFormat(String pattern, String... attributes) {
        timestampFormat(new SimpleDateFormat(pattern), attributes);
    }

    /**
     * Registers timestamp converters (Date -> String -> java.sql.Timestamp) for specified model attributes.
     */
    public void timestampFormat(DateFormat format, String... attributes) {
        convertWith(new DateToStringConverter(format), attributes);
        convertWith(new StringToTimestampConverter(format), attributes);
    }

    /**
     * Registers converter for specified model attributes.
     */
    public void convertWith(Converter converter, String... attributes) {
        for (String attribute : attributes) {
            convertWith(converter, attribute);
        }
    }


    /**
     * Returns a value of an attribute. The current implementation  uses reflection to get to
     * a private or public attribute. Subclasses may override this behavior however they like.
     * @param attributeName name of attribute. For a standard class it would be an actual name  of a field retrievable
     *                      by reflection.
     *
     * @return value of attribute.
     */
    public Object get(String attributeName) {
        try {
            boolean needRevert = false;
            Field field = Util.getField(attributeName, this.getClass());
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
            throw new ValidationException(this);
        }
    }

    public void addFailedValidator(Validator validator, String errorKey) {
        errors.addValidator(errorKey, validator);
    }

    public boolean isValid() {
        validate();
        return errors.size() == 0;
    }


    /**
     * Runs all registered validators and collects errors if any.
     */
    public void validate() {
        validate(true);
    }

    /**
     * Runs all registered validators and collects errors if any.
     *
     * @param reset true to reset all previous validation errors.
     */
    public void validate(boolean reset) {
        if(reset){
            errors = new Errors();
        }

        for (Validator validator : validators()) {
            validator.validate(this);
        }
    }

    /**
     * Provides an instance of <code>Errors</code> object, filled with error messages after validation.
     *
     * @return an instance of <code>Errors</code> object, filled with error messages after validation.
     */
    public Errors errors() {
        //TODO: needs to   clone before returning?
        return errors;
    }

    /**
     * Provides an instance of localized <code>Errors</code> object, filled with error messages after validation.
     *
     * @param locale locale.
     * @return an instance of localized <code>Errors</code> object, filled with error messages after validation.
     */
    public Errors errors(Locale locale) {
        errors.setLocale(locale);
        return errors;
    }
}
