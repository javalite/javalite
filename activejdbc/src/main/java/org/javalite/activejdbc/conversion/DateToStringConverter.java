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

package org.javalite.activejdbc.conversion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Converts instances of {@link java.util.Date} to {@link String}.
 *
 * @author ericbn
 */
public class DateToStringConverter implements Converter<java.util.Date, String> {

    private final DateFormat format;

    /**
     * @param pattern pattern to use for conversion
     */
    public DateToStringConverter(String pattern) {
        this(new SimpleDateFormat(pattern));
    }
    /**
     * @param format DateFormat to use for conversion
     */
    public DateToStringConverter(DateFormat format) {
        this.format = format;
    }

    /**
     * @param sourceClass source Class
     * @param destinationClass destination Class
     * @return true if sourceClass is java.util.Date, or a subclass of it, and destinationClass is String
     */
    @Override
    public boolean canConvert(Class sourceClass, Class destinationClass) {
        return java.util.Date.class.isAssignableFrom(sourceClass) && String.class.equals(destinationClass);
    }

    /**
     * @param source instance of java.util.Date, or subclasses of it, for example:
     * java.sql.Date, java.sql.Time, java.sql.Timestamp
     * @return source converted to String
     */
    @Override
    public String convert(java.util.Date source) {
        return format.format(source);
    }
}
