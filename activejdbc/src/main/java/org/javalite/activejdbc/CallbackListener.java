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
public interface  CallbackListener<T extends Model> {

    void afterLoad(T m);

    void beforeSave(T m);
    void afterSave(T m);

    void beforeCreate(T m);
    void afterCreate(T m);
    
    void beforeUpdate(T m);
    void afterUpdate(T m);

    void beforeDelete(T m);
    void afterDelete(T m);

    void beforeValidation(T m);
    void afterValidation(T m);
}