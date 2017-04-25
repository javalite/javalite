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

import org.javalite.activejdbc.CallbackAdapter;
import org.javalite.activejdbc.Model;
import org.javalite.common.Util;

/**
 * @author Igor Polevoy
 */
public class PlantCallback extends CallbackAdapter {

    public boolean
            beforeSaveCalled = false, afterSaveCalled = false,
            beforeCreateCalled = false, afterCreateCalled = true,
            beforeDeleteCalled = false, afterDeleteCalled = false,
            beforeValidationCalled = false, afterValidationCalled = false;

    private static final PlantCallback CALLBACK = new PlantCallback();
    private PlantCallback(){}

    public static PlantCallback instance(){
        return CALLBACK;
    }
    @Override
    public void beforeSave(Model m) {
        beforeSaveCalled = true;
    }

    @Override
    public void afterSave(Model m) {
        afterSaveCalled = true;
    }

    @Override
    public void beforeCreate(Model m) {
        beforeCreateCalled = true;
    }

    @Override
    public void afterCreate(Model m) {
        afterCreateCalled = true;
    }

    @Override
    public void beforeDelete(Model m) {
        beforeDeleteCalled = true;
    }

    @Override
    public void afterDelete(Model m) {
        afterDeleteCalled = true;
    }

    @Override
    public void beforeValidation(Model m) {
        beforeValidationCalled = true;
        if(Util.blank(m.get("category"))){
            m.set("category", "none");
        }
    }

    @Override
    public void afterValidation(Model m) {
        afterValidationCalled = true;
    }
}
