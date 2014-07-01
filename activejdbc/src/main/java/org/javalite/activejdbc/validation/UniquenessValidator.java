/*
Copyright 2009-2014 Igor Polevoy

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

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.Registry;


public class UniquenessValidator extends ValidatorAdapter {
    private String attribute;
    public UniquenessValidator(String attribute) {
        this.attribute = attribute;
        setMessage("should be unique");
    }
    @Override
    public void validate(Model m) {
        if(Base.count(Registry.instance().getMetaModel(m.getClass()).getTableName(), attribute + " = ? AND id != ?", m.get(attribute), m.getId()) > 0) {
            m.addValidator(this, attribute);
        }
    }
}