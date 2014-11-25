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
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Converts instances of {@link String} to {@link java.sql.Date}.
 *
 * @author ericbn
 */
public class StringToSqlDateConverter extends ConverterAdapter<String, java.sql.Date> {

    private final DateFormat format;

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
     * @return source converted to java.sql.Date
     * @throws ParseException if conversion failed
     */
    @Override
    public java.sql.Date doConvert(String source) throws ParseException {
        return source != null ? new java.sql.Date(format.parse(source).getTime()) : null;
    }
}
