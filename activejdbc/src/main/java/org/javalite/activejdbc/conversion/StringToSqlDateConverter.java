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

package org.javalite.activejdbc.conversion;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.javalite.common.Util.*;

/**
 * Converts instances of {@link String} to {@link java.sql.Date}. This class is thread-safe.
 *
 * @author Eric Nielsen
 */
public class StringToSqlDateConverter extends ConverterAdapter<String, java.sql.Date> {

    private final DateFormat format;
    // Calendar and DateFormat are not thread safe: http://www.javacodegeeks.com/2010/07/java-best-practices-dateformat-in.html
    private final ThreadLocal<DateFormat> threadLocalFormat = new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() {
            return (DateFormat) format.clone();
        }
    };

    /**
     * @param pattern pattern to use for conversion
     */
    public StringToSqlDateConverter(String pattern) {
        this(new SimpleDateFormat(pattern));
    }
    /**
     * @param format DateFormat to use for conversion
     */
    public StringToSqlDateConverter(DateFormat format) {
        this.format = format;
    }

    @Override protected Class<String> sourceClass() { return String.class; }

    @Override protected Class<java.sql.Date> destinationClass() { return java.sql.Date.class; }

    /**
     * @param source instance of String or null
     * @return source converted to java.sql.Date, or null if source is blank
     * @throws ParseException if conversion failed
     */
    @Override
    public java.sql.Date doConvert(String source) throws ParseException {
        return blank(source) ? null : new java.sql.Date(threadLocalFormat.get().parse(source).getTime());
    }
}
