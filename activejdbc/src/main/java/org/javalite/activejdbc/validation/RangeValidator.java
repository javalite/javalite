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

import org.javalite.activejdbc.Model;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Locale;

public class RangeValidator extends ValidatorAdapter {
    private final String attribute;
    private final Number min;
    private final Number max;

    public RangeValidator(String attribute, Number min, Number max){
        this.attribute = attribute;
        this.min =  min;
        this.max = max;

        if (!min.getClass().equals(max.getClass())) {
            throw new IllegalArgumentException("min and max must be the same type");
        }
        this.message = "value should be within limits: > {0} and < {1}";
    }

    @Override
    public void validate(Model m) {
        if(m.get(attribute) == null){
            m.addValidator(this, attribute);
            return;
        }
        Object value = m.get(attribute);
        if(!value.getClass().equals(max.getClass())){
            throw new IllegalArgumentException("attribute " + attribute + " type(class) must be the same type as range limits. Min type: "
                    + min.getClass() + ", Max type: " + max.getClass() + ", Attribute name: " + attribute + ", attribute type: " + value.getClass());
        }

        //I hate code in this class, but it is 1:30 AM... :(
        if(value.getClass().equals(BigDecimal.class) || value.getClass().equals(BigDecimal.class)){
            try {
                Method compareTo = value.getClass().getMethod("compareTo");
                if(((Integer)compareTo.invoke(value, min)) == -1 || ((Integer)compareTo.invoke(value, max)) == 1){
                    m.addValidator(this, attribute);
                }
            } catch (Exception e) {throw new RuntimeException(e);}

        }else{
            if(value.getClass().equals(Byte.class)){
                Byte v = (Byte)value;
                Byte mn = (Byte)min;
                Byte mx = (Byte)max;
                if(v > mx || v < mn)
                    m.addValidator(this, attribute);
            }else if(value.getClass().equals(Double.class)){
                Double v = (Double)value;
                Double mn = (Double)min;
                Double mx = (Double)max;
                if(v > mx || v < mn)
                    m.addValidator(this, attribute);
            }else if(value.getClass().equals(Float.class)){
                Float v = (Float)value;
                Float mn = (Float)min;
                Float mx = (Float)max;
                if(v > mx || v < mn)
                    m.addValidator(this, attribute);
            }else if(value.getClass().equals(Integer.class)){
                Integer v = (Integer)value;
                Integer mn = (Integer)min;
                Integer mx = (Integer)max;
                if(v > mx || v < mn)
                    m.addValidator(this, attribute);
            }else if(value.getClass().equals(Long.class)){
                Long v = (Long)value;
                Long mn = (Long)min;
                Long mx = (Long)max;
                if(v > mx || v < mn)
                    m.addValidator(this, attribute);
            }else if(value.getClass().equals(Short.class)){
                Short v = (Short)value;
                Short mn = (Short)min;
                Short mx = (Short)max;
                if(v > mx || v < mn)
                    m.addValidator(this, attribute);
            }
        }
    }

    @Override
    public String formatMessage(Locale locale, Object ... params) {//params not used
        return super.formatMessage(locale, min, max);
    }
}
