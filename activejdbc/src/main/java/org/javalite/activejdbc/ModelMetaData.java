/*
Copyright 2009-2014 Igor Polevoy

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

/**
 * Stores metadata for a Model: converters, etc.
 *
 * @author ericbn
 */
class ModelMetaData {
    private final Map<String, List<Converter>> attributeConverters = new CaseInsensitiveMap<List<Converter>>();

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
        addConverter(new DateToStringConverter(format), attributes);
        addConverter(new StringToSqlDateConverter(format), attributes);
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
        addConverter(new DateToStringConverter(format), attributes);
        addConverter(new StringToTimestampConverter(format), attributes);
    }

    /**
     * Registers converter for specified model attributes.
     */
    void addConverter(Converter converter, String... attributes) {
        for (String attribute : attributes) {
            addConverter(converter, attribute);
        }
    }

    /**
     * Registers converter for specified model attribute.
     */
    void addConverter(Converter converter, String attribute) {
        List<Converter> list = attributeConverters.get(attribute);
        if (list == null) {
            list = new ArrayList<Converter>();
            attributeConverters.put(attribute, list);
        }
        list.add(converter);
    }

    /**
     * Returns converter for specified model attribute, able to convert from sourceClass to destinationClass.
     * Returns null if no suitable converter was found.
     */
    <S, T> Converter<S, T> getConverterForClass(String attribute, Class<S> sourceClass, Class<T> destinationClass) {
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
    <T> Converter<Object, T> getConverterForValue(String attribute, Object value, Class<T> destinationClass) {
        return getConverterForClass(attribute,
                value != null ? (Class<Object>) value.getClass() : Object.class, destinationClass);
    }
}
