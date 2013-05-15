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

import org.javalite.common.Convert;


/**
 * @author Igor Polevoy
 */
public class SimpleFormatter implements Formatter{

    private Class clazz;
    private String prefix, suffix;

    public SimpleFormatter(Class clazz, String prefix, String suffix){
        this.clazz = clazz;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String format(Object value) {
        if(value.getClass() != clazz) throw new IllegalArgumentException("This formatted was configured for: "
                + clazz + ", but you are feeding it: " + value.getClass());

        return prefix + Convert.toString(value) + suffix;
    }

    public Class getValueClass() {
        return clazz;  
    }
}
