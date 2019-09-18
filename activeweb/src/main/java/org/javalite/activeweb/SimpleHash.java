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

package org.javalite.activeweb;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class exists for toString() method.
 * 
 * @author Igor Polevoy
 */
public class SimpleHash extends HashMap<String, String[]> {
    public SimpleHash(Map<String, String[]> parameterMap) {
        super(parameterMap);
    }

    @Override
    public String toString() {
        String[] keys = keySet().toArray(new String[keySet().size()]);
        StringWriter sw = new StringWriter();
        sw.write("{");
        for (int i = 0; i < keys.length; i++) {
            Object key = keys[i];
            sw.write(key + ": " + Arrays.toString(get(key)));
            if(i < keys.length - 1){
                sw.write(", ");
            }
        }
        sw.write("}");
        return sw.toString();
    }
}
