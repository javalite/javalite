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

import activejdbc.Model;
import javalite.common.Util;

import java.text.SimpleDateFormat;
import java.sql.Timestamp;

/**
 * @author Igor Polevoy
 */
public class TimestampConverter extends Converter{

    private String attributeName, message;
    private SimpleDateFormat df;

    public TimestampConverter(String attributeName, String format){
        this.attributeName = attributeName;
        this.message = "attribute " + attributeName + " does not conform to format: " + format;
        df = new SimpleDateFormat(format);
    }

    public void convert(Model m) {

        Object val = m.get(attributeName);
        if(!Util.blank(val)){
            try{
                long time = df.parse(val.toString()).getTime();
                Timestamp t = new Timestamp(time);
                m.set(attributeName, t);
            }
            catch(Exception e){
                m.addError(attributeName, message + ", current value: '" + val + "'");
            }
        }
    }

    public void setMessage(String message) {
         this.message = message;
    }
}