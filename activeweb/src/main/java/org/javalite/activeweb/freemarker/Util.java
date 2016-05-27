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

package org.javalite.activeweb.freemarker;

import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class Util {

    /**
     * Will throw {@link IllegalArgumentException} if a key in the map is missing.
     * If a map does nto have all the passed in keys in it, this method will throw exception. 
     *
     * @param params map with values
     * @param keys  list of keys to check
     */
    protected static void validateParamsPresence(Map params, String... keys) {
        for (String name : keys) {
            if (!params.containsKey(name)) {
                throw new IllegalArgumentException("parameter: '" + name + "' is missing");
            }
        }
    }
}
