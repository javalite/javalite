/*
Copyright 2009-2019 Igor Polevoy

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


package org.javalite.validation;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.javalite.common.Util.blank;

/**
 * Validates presence and correct format of a string.
 * If you are looking for a conversion, see {@link org.javalite.conversion.StringToTimestampConverter} instead.
 *
 * @author Igor Polevoy
 */
public class TimestampValidator extends ValidatorAdapter {

    private String attributeName, format;
    private SimpleDateFormat df;

    // Calendar and DateFormat are not thread safe: http://www.javacodegeeks.com/2010/07/java-best-practices-dateformat-in.html
    private final ThreadLocal<DateFormat> threadLocalFormat = new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() {
            return (DateFormat) df.clone();
        }
    };

    public TimestampValidator(String attributeName, String format){
        this.attributeName = attributeName;
        setMessage("attribute {0} does not conform to format: {1}");
        this.df = new SimpleDateFormat(format);
        this.format = format;
    }

    @Override
    public void validate(Validatable validatable) {
        Object val = validatable.get(attributeName);
        if (!(val instanceof Timestamp) && !blank(val)) {
            try {
                threadLocalFormat.get().parse(val.toString()).getTime();
            } catch(ParseException e) {
                validatable.addFailedValidator(this, attributeName);
            }
        }
    }

    @Override
    public String formatMessage(Locale locale, Object ... params) {//params not used
        return super.formatMessage(locale, attributeName, format);
    }
}