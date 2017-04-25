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


package org.javalite.activejdbc.connection_config;

import javax.naming.InitialContext;

/**
 * Specification for a JNDI connection.
 *
 * @author Igor Polevoy
 */
public class ConnectionJndiSpec implements ConnectionSpec {
    private final InitialContext context;
    private final String dataSourceJndiName;

    public ConnectionJndiSpec(String dataSourceJndiName) {
        this.context = null;
        this.dataSourceJndiName = dataSourceJndiName;
    }

    public ConnectionJndiSpec(InitialContext context) {
        this.context = context;
        this.dataSourceJndiName = null;
    }

    public String getDataSourceJndiName() {
        return dataSourceJndiName;
    }

    /**
     * @deprecated unused method, returns null
     */
    @Deprecated
    public String getUser() {
        return null;
    }

    /**
     * @deprecated unused method, returns null
     */
    @Deprecated
    public String getPassword() {
        return null;
    }

    public InitialContext getContext() {
        return context;
    }
}
