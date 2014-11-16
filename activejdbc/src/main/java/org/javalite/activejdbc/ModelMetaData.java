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
import org.javalite.activejdbc.convertion.Converter;
import org.javalite.activejdbc.convertion.DateToStringConverter;
import org.javalite.activejdbc.convertion.StringToSqlDateConverter;
import org.javalite.activejdbc.convertion.StringToTimestampConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ModelMetaData {
    private final static Logger logger = LoggerFactory.getLogger(ModelMetaData.class);

    private final Map<String, List<Converter>> attributeConverters = new HashMap<String, List<Converter>>();

    void addDateConverters(String pattern, String... attributes) {
        addDateConverters(new SimpleDateFormat(pattern), attributes);
    }

    void addDateConverters(DateFormat format, String... attributes) {
        Converter from = new DateToStringConverter(format);
        Converter to = new StringToSqlDateConverter(format);
        for (String attribute : attributes) {
            addConverter(from, attribute);
            addConverter(to, attribute);
        }
    }

    void addTimestampConverters(String pattern, String... attributes) {
        addTimestampConverters(new SimpleDateFormat(pattern), attributes);
    }

    void addTimestampConverters(DateFormat format, String... attributes) {
        Converter from = new DateToStringConverter(format);
        Converter to = new StringToTimestampConverter(format);
        for (String attribute : attributes) {
            addConverter(from, attribute);
            addConverter(to, attribute);
        }
    }

    private void addConverter(Converter converter, String attribute) {
        List<Converter> list = attributeConverters.get(attribute);
        if (list == null) {
            list = new ArrayList<Converter>();
            attributeConverters.put(attribute, list);
        }
        list.add(converter);
    }

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
