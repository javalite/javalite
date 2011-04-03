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
package app.config;

import activeweb.AbstractDBConfig;

/**
 * @author Igor Polevoy
 */
public class DbConfig extends AbstractDBConfig {

    public void init() {

        environment("development").jndi("jdbc/kitchensink_development");
        
        environment("development").testing().jdbc("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/kitchensink_test", "root", "p@ssw0rd");

        environment("hudson").testing().jdbc("com.mysql.jdbc.Driver", "jdbc:mysql://172.30.64.31/kitchensink_test", "root", "p@ssw0rd");

        environment("production").jndi("jdbc/kitchensink_production");        
    }
}
