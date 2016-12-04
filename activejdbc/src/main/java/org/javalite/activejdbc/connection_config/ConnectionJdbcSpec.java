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

import java.util.Properties;

/**
 * Specification for a JDBC connection
 *
 * @author Igor Polevoy
 */
public class ConnectionJdbcSpec implements ConnectionSpec {
    private final String driver;
    private final String url;
    private final String user;
    private final String password;
    private final Properties properties;

    public ConnectionJdbcSpec(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.properties = null;
    }

    public ConnectionJdbcSpec(String driver, String url, Properties properties) {
        this.driver = driver;
        this.url = url;
        this.user = null;
        this.password = null;
        this.properties = properties;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public Properties getProps() {
        return properties;
    }
}
