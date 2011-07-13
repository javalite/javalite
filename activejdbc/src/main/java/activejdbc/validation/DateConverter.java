/*
Copyright 2009-2010 Igor Polevoy 

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


package activejdbc.validation;

import activejdbc.Messages;
import activejdbc.Model;
import javalite.common.Util;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Igor Polevoy
 */
public class DateConverter extends Converter{

    private String attributeName, message, format;
    private SimpleDateFormat df;

    public DateConverter(String attributeName, String format){
        this.attributeName = attributeName;
        this.message = "attribute {0} does not conform to format: {1}";
        df = new SimpleDateFormat(format);
        this.format = format;
    }
    
    public void convert(Model m) {

        Object val = m.get(attributeName);
        if(!Util.blank(val)){
            try{
                long time = df.parse(val.toString()).getTime();
                java.sql.Date d = new java.sql.Date(time);
                m.set(attributeName, d);
            }
            catch(Exception e){
                m.addValidator(this, attributeName);
            }
        }
    }

    public void setMessage(String message) {
         this.message = message;
    }

    public String formatMessage(Locale locale, Object ... params) {//params not used
        return  locale != null ? Messages.message(message, locale, attributeName, format)
                : Messages.message(message, attributeName, format); 
    }
}
