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

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class RegexpValidator implements Validator{

    protected String message = "value does not match given format";
    private String rule;
    private String attribute;

    public RegexpValidator(String attribute, String rule){
        this.rule = rule;
        this.attribute = attribute;
    }
    public void validate(Model m) {
        if(m.get(attribute) == null){
            m.addError(attribute, message);
            return;
        }
        Object value = m.get(attribute);
        if(!value.getClass().equals(String.class))
            throw new IllegalArgumentException("attribute " + attribute + " is not String");

        Pattern pattern = Pattern.compile(rule, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value.toString());
        if(!matcher.matches()){
           m.addError(attribute, message);
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
