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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;

/**
 * Facade to HTTP session. 
 *
 * @author Igor Polevoy
 */
public class SessionFacade implements Map {

    /**
     * Returns a session ID from underlying session.
     *
     * @return a session ID from underlying session, or null if session does not exist.
     */
    public String id(){
        HttpServletRequest r = RequestContext.getHttpRequest();
        if(r == null){
            return null;
        }
        HttpSession session = r.getSession(false);
        return session == null ? null : session.getId();
    }


    /**
     * Retrieve object from session.
     *
     * @param name name of object.
     * @return named object. 
     */
    public Object get(String name){
        return RequestContext.getHttpRequest().getSession(true).getAttribute(name);
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
    public Object remove(String name){
        return remove((Object)name);

//        Object val = get(name);
//        RequestContext.getHttpRequest().getSession(true).removeAttribute(name);
//        return val;
    }

    /**
     * Add object to a session.
     * @param name name of object
     * @param value object reference.
     */
    public Object put(String name, Serializable value){
        Object val = RequestContext.getHttpRequest().getSession(true).getAttribute(name);
        RequestContext.getHttpRequest().getSession(true).setAttribute(name, value);
        return val;
    }

    /**
     * Returns time when session was created. 
     *
     * @return time when session was created.
     */
    public long getCreationTime(){
        return RequestContext.getHttpRequest().getSession(true).getCreationTime();
    }

    /**
     * Invalidates current session. All attributes are discarded.
     */
    public void invalidate(){
        RequestContext.getHttpRequest().getSession(true).invalidate();
    }

    /**
     * Sets time to live in seconds.
     * @param seconds time to live.
     */
    public void setTimeToLive(int seconds){
        RequestContext.getHttpRequest().getSession(true).setMaxInactiveInterval(seconds);
    }

    /**
     * Returns names of current attributes as a list.
     *
     * @return names of current attributes as a list.
     */
    public String[] names(){
        List<String> namesList = new ArrayList<>();
        Enumeration names = RequestContext.getHttpRequest().getSession(true).getAttributeNames();
        while (names.hasMoreElements()) {
            Object o = names.nextElement();
            namesList.add(o.toString());
        }        
        return namesList.toArray(new String[namesList.size()]);
    }


    /**
     * returns ID of the underlying session
     *
     * @return ID of the underlying session
     */
    public String getId(){
        return RequestContext.getHttpRequest().getSession(true).getId();
    }


    /**
     * Destroys current session
     */
    public void destroy(){
        RequestContext.getHttpRequest().getSession(true).invalidate();
    }



    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return !RequestContext.getHttpRequest().getSession(true).getAttributeNames().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key) {
        return RequestContext.getHttpRequest().getSession(true).getAttribute(key.toString()) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Enumeration names = RequestContext.getHttpRequest().getSession(true).getAttributeNames();
        while (names.hasMoreElements()){
            String name = names.nextElement().toString();
            if(name.equals(value)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        return get(key.toString());
    }

    @Override
    public Object put(Object key, Object value) {
        Object val = get(key.toString());
        put(key.toString(), (Serializable)value);
        return val;
    }

    @Override
    public Object remove(Object key) {
        Object val = get(key.toString());
        RequestContext.getHttpRequest().getSession(true).removeAttribute(key.toString());
        return val;
    }

    @Override
    public void putAll(Map m) {
        Set keys = m.keySet();
        for(Object k:keys){
            put(k, m.get(k));
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Object> keySet() {
        Set<Object> keys = new HashSet<>();
        Enumeration names = RequestContext.getHttpRequest().getSession(true).getAttributeNames();
        while (names.hasMoreElements()){
            Object name = names.nextElement();
            keys.add(name);
        }
        return keys;
    }

    @Override
    public Collection values() {
        Set<Object> values = new HashSet<>();
        Enumeration names = RequestContext.getHttpRequest().getSession(true).getAttributeNames();
        while (names.hasMoreElements()){
            Object name = names.nextElement();
            values.add(get(name));
        }
        return values;
    }

    @Override
    public Set<Entry> entrySet() {
        throw new UnsupportedOperationException();
    }
}
