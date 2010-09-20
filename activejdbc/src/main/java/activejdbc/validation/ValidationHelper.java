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
import activejdbc.Registry;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class, only exists to pare down the Model class.
 * 
 * @author Igor Polevoy
 */
public class ValidationHelper {
    public static NumericValidationBuilder addNumericalityValidators(Class<Model> modelClass, String... attributes) {
        List<NumericValidator> validators = new ArrayList<NumericValidator>();

        for (String attribute : attributes) {
            validators.add(new NumericValidator(attribute));
        }
        Registry.instance().addValidators(modelClass, validators);
        return new NumericValidationBuilder(validators);
    }


     public static ValidationBuilder addRegexpValidator(Class<Model> modelClass, String attribute, String pattern) {
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(new RegexpValidator(attribute, pattern));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

     public static ValidationBuilder addValidator(Class<Model> modelClass, Validator validator) {
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(validator);
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    public static ValidationBuilder addEmailValidator(Class<Model> modelClass, String attribute) {
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(new EmailValidator(attribute));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    public static ValidationBuilder addRangevalidator(Class<Model> modelClass, String attribute, Number min, Number max) {
        List<Validator> validators = new ArrayList<Validator>();

        validators.add(new RangeValidator(attribute, min, max));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    public static ValidationBuilder addPresensevalidators(Class<Model> modelClass, String... attributes) {
        List<Validator> validators = new ArrayList<Validator>();

        for (String attribute : attributes) {
            validators.add(new AttributePresenceValidator(attribute));
        }
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    public static ValidationBuilder addDateConverter(Class<Model> modelClass, String attributeName, String format){
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(new DateConverter(attributeName, format));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    public static ValidationBuilder addTimestampConverter(Class<Model> modelClass, String attributeName, String format){
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(new TimestampConverter(attributeName, format));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }
}
