/*
Copyright 2009-2016 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.activejdbc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.javalite.activejdbc.conversion.Converter;
import org.javalite.activejdbc.conversion.DateToStringConverter;
import org.javalite.activejdbc.conversion.StringToSqlDateConverter;
import org.javalite.activejdbc.conversion.StringToTimestampConverter;
import org.javalite.activejdbc.validation.AttributePresenceValidator;
import org.javalite.activejdbc.validation.NumericValidationBuilder;
import org.javalite.activejdbc.validation.NumericValidator;
import org.javalite.activejdbc.validation.ValidationBuilder;
import org.javalite.activejdbc.validation.Validator;

/**
 * Stores metadata for a Model: converters, etc.
 *
 * @author ericbn
 */
class ModelRegistry {
    private final List<CallbackListener> callbacks = new ArrayList<>();
    private final Map<String, List<Converter>> attributeConverters = new CaseInsensitiveMap<>();
    private final List<Validator> validators = new ArrayList<>();

    void callbackWith(CallbackListener... listeners) {
        callbackWith(Arrays.asList(listeners));
    }

    void callbackWith(Collection<CallbackListener> callbacks) {
        this.callbacks.clear();
        this.callbacks.addAll(callbacks);
    }

    List<CallbackListener> callbacks() {
        return callbacks;
    }

    /**
     * Registers date converters (Date -> String -> java.sql.Date) for specified model attributes.
     */
    void dateFormat(String pattern, String... attributes) {
        dateFormat(new SimpleDateFormat(pattern), attributes);
    }

    /**
     * Registers date converters (Date -> String -> java.sql.Date) for specified model attributes.
     */
    void dateFormat(DateFormat format, String... attributes) {
        convertWith(new DateToStringConverter(format), attributes);
        convertWith(new StringToSqlDateConverter(format), attributes);
    }

    /**
     * Registers timestamp converters (Date -> String -> java.sql.Timestamp) for specified model attributes.
     */
    void timestampFormat(String pattern, String... attributes) {
        timestampFormat(new SimpleDateFormat(pattern), attributes);
    }

    /**
     * Registers timestamp converters (Date -> String -> java.sql.Timestamp) for specified model attributes.
     */
    void timestampFormat(DateFormat format, String... attributes) {
        convertWith(new DateToStringConverter(format), attributes);
        convertWith(new StringToTimestampConverter(format), attributes);
    }

    /**
     * Registers converter for specified model attributes.
     */
    void convertWith(Converter converter, String... attributes) {
        for (String attribute : attributes) {
            convertWith(converter, attribute);
        }
    }

    /**
     * Registers converter for specified model attribute.
     */
    void convertWith(Converter converter, String attribute) {
        List<Converter> list = attributeConverters.get(attribute);
        if (list == null) {
            list = new ArrayList<>();
            attributeConverters.put(attribute, list);
        }
        list.add(converter);
    }

    /**
     * Returns converter for specified model attribute, able to convert from sourceClass to destinationClass.
     * Returns null if no suitable converter was found.
     */
    <S, T> Converter<S, T> converterForClass(String attribute, Class<S> sourceClass, Class<T> destinationClass) {
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
    <T> Converter<Object, T> converterForValue(String attribute, Object value, Class<T> destinationClass) {
        return converterForClass(attribute,
                value != null ? (Class<Object>) value.getClass() : Object.class, destinationClass);
    }

    ValidationBuilder validateWith(Validator validator) {
        validators.add(validator);
        return new ValidationBuilder(validator);
    }

    ValidationBuilder validateWith(List<Validator> list) {
        this.validators.addAll(list);
        return new ValidationBuilder(list);
    }

    NumericValidationBuilder validateNumericalityOf(String... attributes) {
        List<NumericValidator> list = new ArrayList<>();
        for (String attribute : attributes) {
            NumericValidator validator = new NumericValidator(attribute);
            list.add(validator);
            validators.add(validator);
        }
        return new NumericValidationBuilder(list);
    }

    ValidationBuilder validatePresenceOf(String... attributes) {
        List<Validator> list = new ArrayList<>();
        for (String attribute : attributes) {
            list.add(new AttributePresenceValidator(attribute));
        }
        return validateWith(list);
    }

    void removeValidator(Validator validator) {
        validators.remove(validator);
    }

    List<Validator> validators() {
        return validators;
    }
}
