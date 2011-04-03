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

import activejdbc.ConnectionJdbcSpec;
import activejdbc.ConnectionJndiSpec;
import java.util.Properties;


/**
 * @author Igor Polevoy
 */
public class ConnectionBuilder {
    
    private ConnectionSpecWrapper connectionWrapper;
    
    ConnectionBuilder(String environment) {
        connectionWrapper = new ConnectionSpecWrapper();
        connectionWrapper.setEnvironment(environment);
        Configuration.instance().addConnectionWrapper(connectionWrapper);
    }

    public void jndi(String jndi) {
        connectionWrapper.setConnectionSpec(new ConnectionJndiSpec(jndi));
    }

    public void jdbc(String driver, String url, String user, String password) {
        connectionWrapper.setConnectionSpec(new ConnectionJdbcSpec(driver, url, user, password));
    }

    public void jdbc(String driver, String url, Properties props) {
        connectionWrapper.setConnectionSpec(new ConnectionJdbcSpec(driver, url, props));
    }

    public ConnectionBuilder db(String dbName) {
        connectionWrapper.setDbName(dbName);
        return this;
    }

    public ConnectionBuilder testing() {
        connectionWrapper.setTesting(true);
        return this;
    }    

}
