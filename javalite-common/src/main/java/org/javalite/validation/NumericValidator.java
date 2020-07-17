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

import org.javalite.common.ConversionException;
import org.javalite.common.Convert;

import java.text.NumberFormat;
import java.text.ParsePosition;

public class NumericValidator extends ValidatorAdapter {
    private final String attribute;

    private Double min;
    private Double max;
    private boolean allowNull, onlyInteger;



    public NumericValidator(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public void validate(Validatable validatable) {

        Object value = validatable.get(attribute);

        if(!present(value)){
            setMessage("value is missing");
            validatable.addFailedValidator(this, attribute);
            return;
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
                    validatable.addFailedValidator(this, attribute);
                    setMessage("value is not a number");
                }
            } else {
                validatable.addFailedValidator(this, attribute);
            }
        }

        if(min != null){
            try{
                validateMin(Convert.toDouble(value), validatable);
            }catch(ConversionException e){
                validatable.addFailedValidator(this, attribute);
                setMessage(e.getMessage());
            }
        }

        if(max != null){
            try{
                validateMax(Convert.toDouble(value), validatable);
            }catch(ConversionException e){
                validatable.addFailedValidator(this, attribute);
                setMessage(e.getMessage());
            }
        }

        if(onlyInteger){
            validateIntegerOnly(value, validatable);
        }
    }

    private void validateMin(Double value, Validatable validatable){

        if(value <= min){
            setMessage("value is less than " + min);
            validatable.addFailedValidator(this, attribute);
        }
    }

    private boolean present(Object value){

        if(allowNull){
            return true;
        }
        return value != null;
    }

    private void validateIntegerOnly(Object value, Validatable validatable){
        try{
            Integer.valueOf(value.toString());
        } catch(NumberFormatException e) {
            setMessage("value is not an integer");
            validatable.addFailedValidator(this, attribute);
        }
    }



    private void validateMax(Double value, Validatable validatable){
        if(value >= max){
            setMessage("value is greater than " + max);
            validatable.addFailedValidator(this, attribute);
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

}
