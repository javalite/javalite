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

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class NumericValidationBuilder extends ValidationBuilder<NumericValidator>{

    public NumericValidationBuilder(List<NumericValidator> validators){
        super(validators);
    }

    /**
     * Will ignore validation if set to <code>true</code>. Default is <code>false</code>.
     *
     * @param allow set to true to ignore validation if value if <code>null</code>.
     * @return NumericValidationBuilder
     */
    public NumericValidationBuilder allowNull(boolean allow){
        for(NumericValidator validator:validators){
            validator.setAllowNull(allow);
        }
        return this;
    }

    /**
     * Will invalidate the value if it is not integer.
     *
     * @return NumericValidationBuilder.
     */
    public NumericValidationBuilder onlyInteger(){
        for(NumericValidator validator:validators){
            validator.setOnlyInteger(true);
        }
        return this;
    }

    /**
     * Specify a upper bound for a value, not inclusive.
     *
     * @param max lower bound for numeric value.
     * @return NumericValidationBuilder.
     */
    public NumericValidationBuilder lessThan(double max){
        for(NumericValidator validator:validators){
            validator.setMax(max);
        }
        return this;
    }

    /**
     * Specify a lower bound for a value, not inclusive.
     *
     * @param min lower bound for numeric value.
     * @return NumericValidationBuilder.
     */
    public NumericValidationBuilder greaterThan(double min){
        for(NumericValidator validator:validators){
            validator.setMin(min);
        }
        return this;
    }

    /**
     * Converts an empty string to null before validation.
     * This method is useful in web applications when an HTTP requests
     * sends in a form with inputs that are not filled because they are optional.
     *
     * @return NumericValidationBuilder.
     * @deprecated use {@link org.javalite.activejdbc.Model#blankToNull(java.lang.String...)} instead
     */
    @Deprecated
    public NumericValidationBuilder convertNullIfEmpty(){
        for(NumericValidator validator:validators){
            validator.convertNullIfEmpty(true);
        }
        return this;
    }
}
