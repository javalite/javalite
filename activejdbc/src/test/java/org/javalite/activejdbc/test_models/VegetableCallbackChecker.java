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


package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.test.jspec.TestException;

/**
 * @author Igor Polevoy
 */
public class VegetableCallbackChecker {

    public boolean beforeValidationCalled = false, afterValidationCalled = false;
    protected void checkBeforeValidation(Model m){
        beforeValidationCalled = true;
           if(m.errors().size() != 0)
                throw new TestException("model before validation must not have errors");
    }
    protected void checkAfterValidation(Model m){
        afterValidationCalled = true;
        if(m.errors().size() == 0)
                throw new TestException("model after validation must have errors");
    }
}