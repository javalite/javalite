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

/**
 * @author Igor Polevoy
 */
public class Fruit extends Model {
    FruitCallbackChecker cc;
    static{
        validatePresenceOf("fruit_name");
    }

    public Fruit(FruitCallbackChecker cc){
        this.cc = cc;
    }

    @Override
    protected void beforeSave() {
        cc.checkBeforeSave(this);
    }

    @Override
    protected void beforeCreate() {
        cc.checkBeforeCreate(this);
    }

    @Override
    protected void afterCreate() {
        cc.checkAfterCreate(this);
    }

    @Override
    protected void afterSave() {
        cc.checkAfterSave(this);
    }

    @Override
    protected void beforeDelete() {
        cc.checkBeforeDelete(this);
    }

    @Override
    protected void afterDelete() {
        cc.checkAfterDelete(this);
    }
}
