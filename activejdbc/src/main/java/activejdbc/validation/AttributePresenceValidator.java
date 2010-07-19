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

public class AttributePresenceValidator implements Validator {

    private String attribute, message = "value is missing";

    public AttributePresenceValidator(String attribute) {
        this.attribute = attribute;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void validate(Model m) {
        if (m.get(attribute) == null || m.get(attribute).equals("")) {
            //TODO: use resource bundles for messages
            m.addError(attribute, message);
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
