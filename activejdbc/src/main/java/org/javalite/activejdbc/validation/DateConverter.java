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


package org.javalite.activejdbc.validation;

import java.text.ParseException;
import org.javalite.activejdbc.Model;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.javalite.common.Util.*;

/**
 * @author Igor Polevoy
 * @deprecated use {@link org.javalite.activejdbc.conversion.DateToStringConverter} and
 * {@link org.javalite.activejdbc.conversion.StringToSqlDateConverter} instead
 */
@Deprecated
public class DateConverter extends Converter {

    private String attributeName, format;
    private SimpleDateFormat df;

    public DateConverter(String attributeName, String format){
        this.attributeName = attributeName;
        this.message = "attribute {0} does not conform to format: {1}";
        this.df = new SimpleDateFormat(format);
        this.format = format;
    }

    @Override
    public void convert(Model m) {
        Object val = m.get(attributeName);
        if (!(val instanceof java.util.Date) && !blank(val)) {
            try {
                long time = df.parse(val.toString()).getTime();
                java.sql.Date d = new java.sql.Date(time);
                m.set(attributeName, d);
            } catch (ParseException e) {
                m.addValidator(this, attributeName);
            }
        }
    }

    @Override
    public String formatMessage(Locale locale, Object ... params) {//params not used
        return super.formatMessage(locale, attributeName, format);
    }
}
