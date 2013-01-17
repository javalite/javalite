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


package org.javalite.activejdbc.validation;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.Registry;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class, only exists to pare down the Model class. Method that take Class<Model> should not be called from static initializer,
 * otherwise there is a dead-lock possibility between Registry.init() and Class.forName().
 * 
 * @author Igor Polevoy
 */
public class ValidationHelper {

    @Deprecated
    public static NumericValidationBuilder addNumericalityValidators(Class<Model> modelClass, String... attributes) {
        return addNumericalityValidators(modelClass.getName(), attributes);
    }

    public static NumericValidationBuilder addNumericalityValidators(String modelClass, String... attributes) {
        List<NumericValidator> validators = new ArrayList<NumericValidator>();

        for (String attribute : attributes) {
            validators.add(new NumericValidator(attribute));
        }
        Registry.instance().addValidators(modelClass, validators);
        return new NumericValidationBuilder(validators);
    }

    @Deprecated
    public static ValidationBuilder addRegexpValidator(Class<Model> modelClass, String attribute, String pattern) {
        return addRegexpValidator(modelClass.getName(), attribute, pattern);
    }

    public static ValidationBuilder addRegexpValidator(String modelClass, String attribute, String pattern) {
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(new RegexpValidator(attribute, pattern));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    @Deprecated
    public static ValidationBuilder addValidator(Class<Model> modelClass, Validator validator) {
        return addValidator(modelClass.getName(), validator);
    }

    public static ValidationBuilder addValidator(String modelClass, Validator validator) {
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(validator);
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    @Deprecated
    public static ValidationBuilder addEmailValidator(Class<Model> modelClass, String attribute) {
        return addEmailValidator(modelClass.getName(), attribute);
    }

    public static ValidationBuilder addEmailValidator(String modelClass, String attribute) {
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(new EmailValidator(attribute));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    @Deprecated
    public static ValidationBuilder addRangevalidator(Class<Model> modelClass, String attribute, Number min, Number max) {
        return addRangevalidator(modelClass.getName(), attribute, min, max);
    }

    public static ValidationBuilder addRangevalidator(String modelClass, String attribute, Number min, Number max) {
        List<Validator> validators = new ArrayList<Validator>();

        validators.add(new RangeValidator(attribute, min, max));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    @Deprecated
    public static ValidationBuilder addPresensevalidators(Class<Model> modelClass, String... attributes) {
        return addPresensevalidators(modelClass.getName(), attributes);
    }

    public static ValidationBuilder addPresensevalidators(String modelClass, String... attributes) {
        List<Validator> validators = new ArrayList<Validator>();

        for (String attribute : attributes) {
            validators.add(new AttributePresenceValidator(attribute));
        }
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    @Deprecated
    public static ValidationBuilder addDateConverter(Class<Model> modelClass, String attributeName, String format) {
        return addDateConverter(modelClass.getName(), attributeName, format);
    }

    public static ValidationBuilder addDateConverter(String modelClass, String attributeName, String format) {
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(new DateConverter(attributeName, format));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }

    @Deprecated
    public static ValidationBuilder addTimestampConverter(Class<Model> modelClass, String attributeName, String format) {
        return addTimestampConverter(modelClass.getName(), attributeName, format);
    }

    public static ValidationBuilder addTimestampConverter(String modelClass, String attributeName, String format) {
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(new TimestampConverter(attributeName, format));
        Registry.instance().addValidators(modelClass, validators);
        return new ValidationBuilder(validators);
    }
}
