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

import org.javalite.common.Util;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class TagFactory {
    private String name, body, textAttributes;
    private List<Attribute> attributes = new ArrayList<>();


    public TagFactory(String name, String body) {
        this.name = name;
        this.body = body;
    }

    public void textAttributes(String textAttributes) {
        this.textAttributes = textAttributes;
    }

    public void attribute(String name, String value) {
        attributes.add(new Attribute(name, value));

    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        write(sw);
        return sw.toString();
    }

    public void write(Writer w) {

        try {
            w.write("<");
            w.write(name);
            for (Attribute a : attributes) {
                w.write(" ");
                w.write(a.name());
                w.write("=");
                w.write("\"");
                w.write(a.value());
                w.write("\"");
            }


            if(textAttributes != null){
                w.write(" ");
                w.write(textAttributes);
            }

            if(Util.blank(body)){
                w.write("/>");
            }else{
                w.write(">");
                w.write(body);
                w.write("</");
                w.write(name);
                w.write(">");
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Will add values from params map except the exceptions.
     *
     * @param params map with values. Key used as an attribute name, value as value.
     * @param exceptions list of excepted keys.
     */
    public void addAttributesExcept(Map  params, String ... exceptions){
        List exceptionList = Arrays.asList(exceptions);
        for(Object key: params.keySet()){
            if(!exceptionList.contains(key)){
                attribute(key.toString(), params.get(key).toString());
            }
        }
    }

    class Attribute {
        private String name, value;

        Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String name() {
            return name;
        }

        public String value() {
            return value;
        }
    }
}


