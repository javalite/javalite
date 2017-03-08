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

import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
class SessionHelper {
	
	private SessionHelper() {}
	
    /**
     * Returns all session attributes in a map.
     *
     * @return all session attributes in a map.
     */
     protected static Map<String, Object> getSessionAttributes(){
        //TODO: cache session attributes map since this method can be called multiple times during a request.

        HttpSession session = RequestContext.getHttpRequest().getSession(true);
        Enumeration names = session.getAttributeNames();
        Map<String, Object> values = new HashMap<>();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            values.put(name.toString(), session.getAttribute(name.toString()));
        }
        return values;
    }
}
