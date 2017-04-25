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


package org.javalite.activejdbc.mock;

import javax.naming.*;
import java.util.Hashtable;

/**
 * @author Igor Polevoy
 */
public class MockContext implements Context {
    public Object lookup(Name name) throws NamingException {
        return null;  
    }

    public Object lookup(String name) throws NamingException {
        return new MockDataSource();  
    }

    public void bind(Name name, Object obj) throws NamingException {
        
    }

    public void bind(String name, Object obj) throws NamingException {
        
    }

    public void rebind(Name name, Object obj) throws NamingException {
        
    }

    public void rebind(String name, Object obj) throws NamingException {
        
    }

    public void unbind(Name name) throws NamingException {
        
    }

    public void unbind(String name) throws NamingException {
        
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        
    }

    public void rename(String oldName, String newName) throws NamingException {
        
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return null;  
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return null;  
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return null;  
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return null;  
    }

    public void destroySubcontext(Name name) throws NamingException {
        
    }

    public void destroySubcontext(String name) throws NamingException {
        
    }

    public Context createSubcontext(Name name) throws NamingException {
        return null;  
    }

    public Context createSubcontext(String name) throws NamingException {
        return null;  
    }

    public Object lookupLink(Name name) throws NamingException {
        return null;  
    }

    public Object lookupLink(String name) throws NamingException {
        return null;  
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return null;  
    }

    public NameParser getNameParser(String name) throws NamingException {
        return null;  
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return null;  
    }

    public String composeName(String name, String prefix) throws NamingException {
        return null;  
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return null;  
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return null;  
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return null;  
    }

    public void close() throws NamingException {
        
    }

    public String getNameInNamespace() throws NamingException {
        return null;  
    }
}
    