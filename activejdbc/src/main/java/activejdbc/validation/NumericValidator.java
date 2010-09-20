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

import activejdbc.*;
import activejdbc.Converter;


import java.text.NumberFormat;

public class NumericValidator extends ValidatorAdapter {
    private String attribute;

    private Double min = null;
    private Double max = null;
    private boolean allowNull = false, onlyInteger = false;


    public NumericValidator(String attribute) {
        this.attribute = attribute;
        message = "value is not a number";
    }


    public void validate(Model m) {
        Object value = m.get(attribute);

        if(value == null && allowNull){
            return;
        }


        //this is to check just numericality
        if (value != null) {
            try {
                NumberFormat.getInstance().parse(value.toString());
            } catch (Exception e) {
                m.addValidator(attribute, this);
            }
        } else {
                m.addValidator(attribute, this);
        }

        if(min != null){
            validateMin(Converter.toDouble(value), m);
        }

        if(max != null){
            validateMax(Converter.toDouble(value), m);
        }

        if(onlyInteger){
            validateIntegerOnly(value, m);
        }
    }

    private void validateMin(Double value, Model m){
        if(value <= min){
            m.addValidator(attribute, this);
        }
    }

    private void validateIntegerOnly(Object value, Model m){        
        try{
            Integer.valueOf(value.toString());
        }
        catch(Exception e){
            m.addValidator(attribute, this);
        }
    }



    private void validateMax(Double value, Model m){
        if(value >= max){
            m.addValidator(attribute, this);
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
