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
package org.javalite.activeweb.freemarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class ContentTL {
    private static ThreadLocal<Map<String, List<String>>> contentTL = new ThreadLocal<Map<String, List<String>>>();

    static void reset(){
        contentTL.set(new HashMap<String, List<String>>());
    }
    public static Map<String, List<String>> getAllContent() {
        return contentTL.get();
    }

    static void addContent(String name, String content) {
        if(contentTL.get().get(name) == null){
            contentTL.get().put(name, new ArrayList<String>());
        }
        List<String> contentList = ContentTL.contentTL.get().get(name);
        contentList.add(content);
    }
}