/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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

package org.javalite.activeweb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Instance of this class can hold application-specific objects for duration of application life span.
 *
 * @author Igor Polevoy
 */
public class AppContext {

    private static final AtomicReference<Map<String, Object>> contextReference = new AtomicReference<>(new HashMap<>());

    /**
     * Retrieves object by name
     *
     * @param name name of object
     * @return   object by name;
     */
    public Object get(String name){
        return contextReference.get().get(name);
    }

    /**
     * Retrieves object by name. Convenience generic method. 
     *
     * @param name name of object
     * @param type type requested.
     * @return object by name
     */
    public static  <T>  T get(String name, Class<T> type){
        Object o = contextReference.get().get(name);
        return o == null? null : (T) o;
    }

    /**
     * Sets an application - wide object by name.
     *
     * @param name name of object
     * @param object - instance. 
     */
    public static void set(String name, Object object){
        contextReference.get().put(name, object);
    }
}
