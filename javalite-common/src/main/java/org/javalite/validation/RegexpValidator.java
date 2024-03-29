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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Validator designed for use of regular expressions.
 */
public class RegexpValidator extends ValidatorAdapter {

    private final Pattern pattern;
    private final String attribute;

    /**
     * @param attribute attribute name
     * @param rule regular expression
     * @param flags {@link Pattern}
     */
    public RegexpValidator(String attribute, String rule, int flags) {
        this.pattern = Pattern.compile(rule,  flags);
        this.attribute = attribute;
        setMessage("value does not match given format");
    }

    /**
     * Calls the {@link #RegexpValidator(String, String, int)} with the {@link Pattern#CASE_INSENSITIVE} flag by default.
     *
     * @param attribute attribute name
     * @param rule regular expression
     */
    public RegexpValidator(String attribute, String rule) {
        this(attribute, rule, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void validate(Validatable validatable) {
        if (validatable.get(attribute) == null) {
            validatable.addFailedValidator(this, attribute);
            return;
        }
        Object value = validatable.get(attribute);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("attribute " + attribute + " is not String");
        }
        Matcher matcher = pattern.matcher((String) value);
        if (!matcher.matches()) {
            validatable.addFailedValidator(this, attribute);
        }
    }
}
