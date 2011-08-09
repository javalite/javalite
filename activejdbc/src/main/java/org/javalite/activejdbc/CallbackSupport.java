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


package org.javalite.activejdbc;

import java.util.List;

/**
 * @author Igor Polevoy
 */
class CallbackSupport {

    void fireBeforeSave(Model m){
        beforeSave();
        List<CallbackListener> listeners = Registry.instance().getListeners(m.getClass());
        for(CallbackListener listener: listeners)
            listener.beforeSave(m);
    }

    void fireAfterSave(Model m){
        afterSave();
        List<CallbackListener> listeners = Registry.instance().getListeners(m.getClass());
        for(CallbackListener listener: listeners)
            listener.afterSave(m);
    }

    void fireBeforeCreate(Model m){
        beforeCreate();
        List<CallbackListener> listeners = Registry.instance().getListeners(m.getClass());
        for(CallbackListener listener: listeners)
            listener.beforeCreate(m);
    }

    void fireAfterCreate(Model m){
        afterCreate();
        List<CallbackListener> listeners = Registry.instance().getListeners(m.getClass());
        for(CallbackListener listener: listeners)
            listener.afterCreate(m);
    }

    void fireBeforeDelete(Model m){
        beforeDelete();
        List<CallbackListener> listeners = Registry.instance().getListeners(m.getClass());
        for(CallbackListener listener: listeners)
            listener.beforeDelete(m);
    }

    void fireAfterDelete(Model m){
        afterDelete();
        List<CallbackListener> listeners = Registry.instance().getListeners(m.getClass());
        for(CallbackListener listener: listeners)
            listener.afterDelete(m);
    }

    void fireBeforeValidation(Model m){
        beforeValidation();
        List<CallbackListener> listeners = Registry.instance().getListeners(m.getClass());
        for(CallbackListener listener: listeners)
            listener.beforeValidation(m);
    }
    void fireAfterValidation(Model m){
        afterValidation();
        List<CallbackListener> listeners = Registry.instance().getListeners(m.getClass());
        for(CallbackListener listener: listeners)
            listener.afterValidation(m);        
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
