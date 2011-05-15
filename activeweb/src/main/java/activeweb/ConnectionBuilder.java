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
 * Supports DSL for specifying connection parameters for various environments and modes.
 * This class is not used directly.
 *
 * @author Igor Polevoy
 */
public class ConnectionBuilder {
    
    private ConnectionSpecWrapper connectionWrapper;
    
    ConnectionBuilder(String environment) {
        connectionWrapper = new ConnectionSpecWrapper();
        connectionWrapper.setEnvironment(environment);
        Configuration.addConnectionWrapper(connectionWrapper);
    }

    /**
     * Provide a name of a JNDI datasource configured for runtime.
     * @param jndi name of a JNDI datasource 
     */
    public void jndi(String jndi) {
        connectionWrapper.setConnectionSpec(new ConnectionJndiSpec(jndi));
    }

    /**
     * Configure standard JDBC parameters for opening a connection.
     *
     * @param driver class name of driver
     * @param url JDBC URL
     * @param user user name
     * @param password password
     */
    public void jdbc(String driver, String url, String user, String password) {
        connectionWrapper.setConnectionSpec(new ConnectionJdbcSpec(driver, url, user, password));
    }

    /**
     * Configure expanded JDBC parameters for opening a connection if needed
     *
      @param driver class name of driver
     * @param url JDBC URL
     * @param props properties with additional parameters a driver can take.
     */
    public void jdbc(String driver, String url, Properties props) {
        connectionWrapper.setConnectionSpec(new ConnectionJdbcSpec(driver, url, props));
    }

    /**
     * Name of a database. If this method is not called, the name od database is presumed "default".
     * @param dbName name od database for ActiveJDBC models.
     * @return self
     */
    public ConnectionBuilder db(String dbName) {
        connectionWrapper.setDbName(dbName);
        return this;
    }

    /**
     * Marks this connection to be used for testing. Whe n you use any of the testing classes, such as DBSpec, DBControllerSpec,
     * DNIntegrationSpec and AppIntegrationSpec from activeweb-testing package, they all will use a connection that is marked
     * by this method.
     *
     * @return self
     */
    public ConnectionBuilder testing() {
        connectionWrapper.setTesting(true);
        return this;
    }    
}
