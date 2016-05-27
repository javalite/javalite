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

import static org.javalite.common.Util.*;


public class AttributePresenceValidator extends ValidatorAdapter {

    private final String attribute;

    public AttributePresenceValidator(String attribute) {
        this.attribute = attribute;
        setMessage("value is missing");
    }


    @Override
    public void validate(Model m) {
        if (blank(m.get(attribute))) {
            //TODO: use resource bundles for messages
            m.addValidator(this, attribute);
        }
    }

    @Override
    public int hashCode() {
        return attribute.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!this.getClass().equals(other.getClass())) return false;

        return this.attribute.equals(((AttributePresenceValidator)other).attribute);
    }
}
