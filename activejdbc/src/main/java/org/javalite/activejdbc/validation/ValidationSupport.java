package org.javalite.activejdbc.validation;

import org.javalite.activejdbc.CaseInsensitiveMap;
import org.javalite.activejdbc.conversion.Converter;
import org.javalite.activejdbc.conversion.DateToStringConverter;
import org.javalite.activejdbc.conversion.StringToSqlDateConverter;
import org.javalite.activejdbc.conversion.StringToTimestampConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages validators and converters.
 */
public class ValidationSupport {

    private final Map<String, List<Converter>> attributeConverters = new CaseInsensitiveMap<>();
    private final List<Validator> validators = new ArrayList<>();

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

    public ValidationBuilder validateWith(Validator validator) {
        validators.add(validator);
        return new ValidationBuilder(validator);
    }

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
}
