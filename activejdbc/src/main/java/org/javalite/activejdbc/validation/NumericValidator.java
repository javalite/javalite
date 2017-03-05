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

import org.javalite.common.Convert;
import org.javalite.activejdbc.Model;


import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class NumericValidator extends ValidatorAdapter {
    private final String attribute;

    private Double min;
    private Double max;
    private boolean allowNull, onlyInteger, convertNullIfEmpty;


    public NumericValidator(String attribute) {
        this.attribute = attribute;
        setMessage("value is not a number");
    }

    @Override
    public void validate(Model m) {
        Object value = m.get(attribute);

        if(!present(value, m)){
            return;
        }

        // validators should not also do conversion
        if(convertNullIfEmpty && "".equals(value)){
            m.set(attribute, null);
            value = null;
        }

        if(value == null && allowNull){
            return;
        }

        //this is to check just numericality
        if(!(value instanceof Number)) {
            if (value != null) {
                ParsePosition pp = new ParsePosition(0);
                String input = value.toString();
                // toString() is not Locale dependant...
                // ... but NumberFormat is. For Polish locale where decimal separator is "," instead of ".". Might fail some tests...
                NumberFormat nf = NumberFormat.getInstance();
                nf.setParseIntegerOnly(onlyInteger);
                nf.parse(input, pp);
                if (pp.getIndex() != (input.length())) {
                    m.addValidator(this, attribute);
                }
            } else {
                    m.addValidator(this, attribute);
            }
        }

        if(min != null){
            validateMin(Convert.toDouble(value), m);
        }

        if(max != null){
            validateMax(Convert.toDouble(value), m);
        }

        if(onlyInteger){
            validateIntegerOnly(value, m);
        }
    }

    private void validateMin(Double value, Model m){

        if(value <= min){
            m.addValidator(this, attribute);
        }
    }

    private boolean present(Object value, Model m){

        if(allowNull){
            return true;
        }

        if(value == null){
            setMessage("value is missing");
            m.addValidator(this, attribute);
            return false;
        }else{
            return true;
        }
    }

    private void validateIntegerOnly(Object value, Model m){
        try{
            Integer.valueOf(value.toString());
        } catch(NumberFormatException e) {
            m.addValidator(this, attribute);
        }
    }



    private void validateMax(Double value, Model m){
        if(value >= max){
            m.addValidator(this, attribute);
        }
    }


    public void setMin(Double min){
        this.min = min;
    }

    public void setMax(Double max){
        this.max = max;
    }
    public void setAllowNull(Boolean allowNull){
        this.allowNull = allowNull;
    }

    public void setOnlyInteger(boolean onlyInteger){
        this.onlyInteger = onlyInteger;
    }

    /**
     * @deprecated use {@link org.javalite.activejdbc.conversion.BlankToNullConverter} instead
     */
    @Deprecated
    public void convertNullIfEmpty(boolean convertNullIfEmpty) {
        this.convertNullIfEmpty = convertNullIfEmpty;
    }
}
