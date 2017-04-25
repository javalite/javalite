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
 * Converts instances of {@link String} to {@link java.sql.Timestamp}. This class is thread-safe.
 *
 * @author Eric Nielsen
 */
public class StringToTimestampConverter extends ConverterAdapter<String, java.sql.Timestamp> {

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
    public StringToTimestampConverter(String pattern) {
        this(new SimpleDateFormat(pattern));
    }
    /**
     * @param format DateFormat to use for conversion
     */
    public StringToTimestampConverter(DateFormat format) {
        this.format = format;
    }

    @Override protected Class<String> sourceClass() { return String.class; }

    @Override protected Class<java.sql.Timestamp> destinationClass() { return java.sql.Timestamp.class; }

    /**
     * @param source instance of String or null
     * @return source converted to java.sql.Timestamp, or null if source is blank
     * @throws ParseException if conversion failed
     */
    @Override
    public java.sql.Timestamp doConvert(String source) throws ParseException {
        return blank(source) ? null : new java.sql.Timestamp(threadLocalFormat.get().parse(source).getTime());
    }
}
