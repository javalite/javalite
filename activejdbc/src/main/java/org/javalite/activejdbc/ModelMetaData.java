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
 */
class ModelMetaData {
    private final Map<String, List<Converter>> attributeConverters = new HashMap<String, List<Converter>>();

    /**
     * Registers date converters (Date -> String -> java.sql.Date) for specified model attributes.
     */
    void addDateConverters(String pattern, String... attributes) {
        addDateConverters(new SimpleDateFormat(pattern), attributes);
    }

    /**
     * Registers date converters (Date -> String -> java.sql.Date) for specified model attributes.
     */
    void addDateConverters(DateFormat format, String... attributes) {
        Converter from = new DateToStringConverter(format);
        Converter to = new StringToSqlDateConverter(format);
        for (String attribute : attributes) {
            addConverter(from, attribute);
            addConverter(to, attribute);
        }
    }

    /**
     * Registers timestamp converters (Date -> String -> java.sql.Timestamp) for specified model attributes.
     */
    void addTimestampConverters(String pattern, String... attributes) {
        addTimestampConverters(new SimpleDateFormat(pattern), attributes);
    }

    /**
     * Registers timestamp converters (Date -> String -> java.sql.Timestamp) for specified model attributes.
     */
    void addTimestampConverters(DateFormat format, String... attributes) {
        Converter from = new DateToStringConverter(format);
        Converter to = new StringToTimestampConverter(format);
        for (String attribute : attributes) {
            addConverter(from, attribute);
            addConverter(to, attribute);
        }
    }

    /**
     * Registers converter for specified model attribute.
     */
    private void addConverter(Converter converter, String attribute) {
        List<Converter> list = attributeConverters.get(attribute);
        if (list == null) {
            list = new ArrayList<Converter>();
            attributeConverters.put(attribute, list);
        }
        list.add(converter);
    }

    /**
     * @return converter for specified model attribute, able to convert from sourceClass to destinationClass;
     * returns null if no suitable converter was found.
     */
    <S, T> Converter<S, T> getConverter(String attribute, Class<S> sourceClass, Class<T> destinationClass) {
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
}
