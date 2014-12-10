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


package org.javalite.activejdbc;

/**
 * @author Igor Polevoy
 */
class CallbackSupport {

    void fireBeforeSave(Model m){
        beforeSave();
        for (CallbackListener callback : m.modelRegistryLocal().callbacks()) {
            callback.beforeSave(m);
        }
    }

    void fireAfterSave(Model m){
        afterSave();
        for (CallbackListener callback : m.modelRegistryLocal().callbacks()) {
            callback.afterSave(m);
        }
    }

    void fireBeforeCreate(Model m){
        beforeCreate();
        for (CallbackListener callback : m.modelRegistryLocal().callbacks()) {
            callback.beforeCreate(m);
        }
    }

    void fireAfterCreate(Model m){
        afterCreate();
        for (CallbackListener callback : m.modelRegistryLocal().callbacks()) {
            callback.afterCreate(m);
        }
    }

    void fireBeforeDelete(Model m){
        beforeDelete();
        for (CallbackListener callback : m.modelRegistryLocal().callbacks()) {
            callback.beforeDelete(m);
        }
    }

    void fireAfterDelete(Model m){
        afterDelete();
        for (CallbackListener callback : m.modelRegistryLocal().callbacks()) {
            callback.afterDelete(m);
        }
    }

    void fireBeforeValidation(Model m){
        beforeValidation();
        for(CallbackListener callback: m.modelRegistryLocal().callbacks())
            callback.beforeValidation(m);
    }

    void fireAfterValidation(Model m){
        afterValidation();
        for (CallbackListener callback : m.modelRegistryLocal().callbacks()) {
            callback.afterValidation(m);
        }
    }

    //overridable instance methods
    protected void beforeSave(){}
    protected void afterSave(){}

    protected void beforeCreate(){}
    protected void afterCreate(){}

    protected void beforeDelete(){}
    protected void afterDelete(){}

    protected void beforeValidation(){}
    protected void afterValidation(){}
}
