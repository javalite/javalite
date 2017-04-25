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
public class FruitCallbackChecker {

    public boolean
            beforeSaveCalled = false, afterSaveCalled = false,
            beforeDeleteCalled = false, afterDeleteCalled = false,
            beforeCreateCalled = false, afterCreateCalled = false;


    protected void checkBeforeSave(Model m){
        beforeSaveCalled = true;
        if(m.getId() != null)
            throw new TestException("new model must not have an ID before save()");
    }

    protected void checkBeforeCreate(Model m){
        beforeCreateCalled = true;
        if(m.getId() != null)
            throw new TestException("new model must not have an ID before insert()");
    }

    protected void checkAfterCreate(Model m){
           afterCreateCalled = true;
           if(m.getId() == null)
               throw new TestException("new model must have an ID after insert()");
    }
    
    protected void checkAfterSave(Model m){
        afterSaveCalled = true;
        if(m.getId() == null)
            throw new TestException("new model must have an ID after save()");
    }

    protected void checkBeforeDelete(Model m){
        beforeDeleteCalled = true;
        if(m.isFrozen())
            throw new TestException("new model must not be frozen before delete()");

    }
    protected void checkAfterDelete(Model m){
        afterDeleteCalled = true;
           if(!m.isFrozen())
            throw new TestException("new model must be frozen after delete()");
    }
}
