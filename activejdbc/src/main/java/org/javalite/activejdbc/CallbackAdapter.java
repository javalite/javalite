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


package org.javalite.activejdbc;

/**
 * @author Igor Polevoy
 */
public class CallbackAdapter<T extends Model> implements CallbackListener<T>{

    @Override public void beforeSave(T m) {}

    @Override public void afterLoad(T m) {}

    @Override public void afterSave(T m) {}

    @Override public void beforeCreate(T m) {}

    @Override public void afterCreate(T m) {}

    @Override public void beforeDelete(T m) {}

    @Override public void afterDelete(T m) {}

    @Override public void beforeValidation(T m) {}

    @Override public void afterValidation(T m) {}

    @Override public void beforeUpdate(T m) {}

    @Override public void afterUpdate(T m) {}
}