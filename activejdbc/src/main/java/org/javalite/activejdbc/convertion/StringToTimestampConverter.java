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

package org.javalite.activejdbc.convertion;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class StringToTimestampConverter implements Converter<String, java.sql.Timestamp> {

    private final DateFormat format;

    public StringToTimestampConverter(String pattern) {
        this(new SimpleDateFormat(pattern));
    }
    public StringToTimestampConverter(DateFormat format) {
        this.format = format;
    }

    @Override
    public boolean canConvert(Class sourceClass, Class destinationClass) {
        return String.class.equals(sourceClass) && java.sql.Timestamp.class.equals(destinationClass);
    }

    @Override
    public java.sql.Timestamp convert(String source) {
        try {
            return new java.sql.Timestamp(format.parse(source).getTime());
        } catch (ParseException e) {
            throw new ConvertionException(e);
        }
    }
}
