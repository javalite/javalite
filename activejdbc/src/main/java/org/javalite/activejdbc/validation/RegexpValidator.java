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

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class RegexpValidator extends ValidatorAdapter{

    private final Pattern pattern;
    private final String attribute;

    public RegexpValidator(String attribute, String rule){
        this.pattern = Pattern.compile(rule, Pattern.CASE_INSENSITIVE);
        this.attribute = attribute;
        setMessage("value does not match given format");
    }

    @Override
    public void validate(Model m) {
        if(m.get(attribute) == null){
            m.addValidator(this, attribute);
            return;
        }
        Object value = m.get(attribute);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("attribute " + attribute + " is not String");
        }
        Matcher matcher = pattern.matcher((String) value);
        if(!matcher.matches()){
           m.addValidator(this, attribute);
        }
    }
}
