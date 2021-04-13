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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Facade to HTTP session. 
 *
 * @author Igor Polevoy
 */
public class SessionFacade implements Map<String,Object> {

    private static final String[] EMPTY_ARRAY = new String[0];

    private HttpSession httpSession() {
        HttpServletRequest httpRequest = RequestContext.getHttpRequest();
        return httpRequest == null ? null : httpRequest.getSession(false);
    }

    public boolean isExists() {
        return httpSession() != null;
    }


    /**
     * Returns a session ID from underlying session.
     *
     * @return a session ID from underlying session, or null if session does not exist.
     */
    public String id(){
        HttpSession session = httpSession();
        return session == null ? null : session.getId();
    }

    /**
     * Retrieve object from session.
     *
     * @param name name of object.
     * @return named object.
     */
    @Override
    public Object get(Object name) {
        HttpSession session = httpSession();
        return session == null ? null : session.getAttribute(name.toString());
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
    }

    /**
     * Returns time when session was created. 
     *
     * @return time when session was created or -1 if session is not exists.
     */
    public long getCreationTime(){
        HttpSession session = httpSession();
        return session == null ? -1 : session.getCreationTime();
    }

    /**
     * Invalidates current session. All attributes are discarded.
     */
    public void invalidate(){
        HttpSession session = httpSession();
        if (session != null) {
            session.invalidate();
        }
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
        HttpSession session = httpSession();
        if (session != null) {
            List<String> namesList = new ArrayList<>();
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                Object o = names.nextElement();
                namesList.add(o.toString());
            }
            return namesList.toArray(new String[0]);
        }
        return EMPTY_ARRAY;
    }


    /**
     * returns ID of the underlying session
     *
     * @return ID of the underlying session
     */
    public String getId(){
        return id();
    }


    /**
     * Destroys current session
     */
    public void destroy(){
        invalidate();
    }

    @Override
    public int size() {
        HttpSession session = httpSession();
        if (session != null) {
            Enumeration<String> enumeration = session.getAttributeNames();
            if (enumeration.hasMoreElements()) {
                int size = 0;
                while (enumeration.hasMoreElements()) {
                    enumeration.nextElement();
                    size++;
                }
                return size;
            }
        }
        return 0;
    }

    @Override
    public boolean isEmpty() {
        HttpSession session = httpSession();
        return session == null || session.getAttributeNames().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        HttpSession session = httpSession();
        if (session != null) {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if (name.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add object to a session.
     * @param key name of object
     * @param value object reference.
     */
    @Override
    public Object put(String key, Object value) {
        Object prev = get(key);
        RequestContext.getHttpRequest().getSession(true).setAttribute(key, value);
        return prev;
    }

    @Override
    public Object remove(Object key) {
        HttpSession session = httpSession();
        if (session != null) {
            Object val = session.getAttribute(key.toString());
            session.removeAttribute(key.toString());
            return val;
        }
        return null;
    }

    @Override
    public void putAll(Map m) {
        HttpSession session = RequestContext.getHttpRequest().getSession(true);
        for (Object o : m.entrySet()) {
            Map.Entry entry = (Entry) o;
            session.setAttribute(entry.getKey().toString(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        HttpSession session = httpSession();
        if (session != null) {
            for(String name : keySet()) {
                session.removeAttribute(name);
            }
        }
    }

    @Override
    public Set<String> keySet() {
        HttpSession session = httpSession();
        if (session != null) {
            Set<String> keys = new HashSet<>();
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                keys.add(names.nextElement());
            }
            return keys;
        }
        return Collections.emptySet();
    }

    @Override
    public Collection<Object> values() {
        HttpSession session = httpSession();
        if (session != null) {
            Set<Object> values = new HashSet<>();
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                values.add(session.getAttribute(names.nextElement()));
            }
            return values;
        }
        return Collections.emptySet();
    }

    @Override
    public Set<Entry<String,Object>> entrySet() {
        HttpSession session = httpSession();
        if (session != null) {
            Set<Map.Entry<String,Object>> entries = new HashSet<>();
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                entries.add(new AbstractMap.SimpleImmutableEntry<>(name, session.getAttribute(name)));
            }
            return entries;
        }
        return Collections.emptySet();
    }
}
