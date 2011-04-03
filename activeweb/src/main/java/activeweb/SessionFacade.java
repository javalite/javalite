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
package activeweb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Facade to HTTP session. 
 *
 * @author Igor Polevoy
 */
public class SessionFacade {

    /**
     * Retrieve object from session.
     *
     * @param name name of object.
     * @return named object. 
     */
    public Object get(String name){
        return ContextAccess.getHttpRequest().getSession(true).getAttribute(name);
    }

    /**
     * Convenience method, will do internal check for null and then cast. Will throw  <code>ClassCastException</code>
     * only in case the object in session is different type than expected.
     *
     * @param name name of session object.
     * @param type expected type
     * @return object in session, or null if not found. 
     */
    public <T> T get(String name, Class<T> type){
        return get(name) != null? (T)get(name) : null;
    }


    /**
     * Removes object from session.
     * @param name name of object
     */
    public void remove(String name){
        ContextAccess.getHttpRequest().getSession(true).removeAttribute(name);
    }

    /**
     * Add object to a session.
     * @param name name of object
     * @param value object reference.
     */
    public void put(String name, Serializable value){
        ContextAccess.getHttpRequest().getSession(true).setAttribute(name, value);
    }

    /**
     * Returns time when session was created. 
     *
     * @return time when session was created.
     */
    public long getCreationTime(){
        return ContextAccess.getHttpRequest().getSession(true).getCreationTime();
    }

    /**
     * Invalidates current session. All attributes are discarded.
     */
    public void invalidate(){
        ContextAccess.getHttpRequest().getSession(true).invalidate();
    }

    /**
     * Sets time to live in seconds.
     * @param seconds time to live.
     */
    public void setTimeToLive(int seconds){
        ContextAccess.getHttpRequest().getSession(true).setMaxInactiveInterval(seconds);
    }

    /**
     * Returns names of current attributes as a list.
     *
     * @return names of current attributes as a list.
     */
    public String[] names(){
        List<String> namesList = new ArrayList<String>();
        Enumeration names = ContextAccess.getHttpRequest().getSession(true).getAttributeNames();
        while (names.hasMoreElements()) {
            Object o = names.nextElement();
            namesList.add(o.toString());
        }        
        return namesList.toArray(new String[0]);
    }
}
