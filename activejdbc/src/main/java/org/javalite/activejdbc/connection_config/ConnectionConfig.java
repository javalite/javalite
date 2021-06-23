/*
Copyright 2009-2019 Igor Polevoy

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

import org.javalite.json.JSONHelper;

import static org.javalite.common.Collections.map;

/**
 * Super class for all connection configuration instances
 *
 * @author Igor Polevoy
 */
public abstract class ConnectionConfig {

    private String environment;
    private String dbName = "default";
    private boolean testing;


    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isTesting() {
        return testing;
    }

    public void setTesting(boolean testing) {
        this.testing = testing;
    }

    @Override
    public String toString() {
        return JSONHelper.toJsonString(map("class", getClass(), "environment", environment, "db_name", dbName, "testing", testing));
    }
}