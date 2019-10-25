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

import org.javalite.activejdbc.connection_config.*;

import javax.sql.DataSource;
import java.util.Properties;


/**
 * Supports DSL for specifying connection parameters for various environments and modes.
 * This class is not used directly.
 *
 * @author Igor Polevoy
 */
public class ConnectionBuilder {

    private String environment;
    private String dbName = "default";
    private boolean testing, override = false;

    
    ConnectionBuilder(String environment) {
        this.environment = environment;
    }

    ConnectionBuilder(String environment, boolean override) {
        this.environment = environment;
        this.override = override;

    }

    /**
     * Provide a name of a JNDI datasource configured for runtime.
     * @param jndi name of a JNDI datasource 
     */
    public void jndi(String jndi) {
        ConnectionJndiConfig connectionConfig = new ConnectionJndiConfig(jndi);
        connectionConfig.setDbName(dbName);
        connectionConfig.setEnvironment(environment);
        connectionConfig.setTesting(testing);
        DbConfiguration.addConnectionConfig(connectionConfig, override);
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

        ConnectionJdbcConfig connectionConfig = new ConnectionJdbcConfig(driver, url, user, password);
        connectionConfig.setDbName(dbName);
        connectionConfig.setEnvironment(environment);
        connectionConfig.setTesting(testing);
        DbConfiguration.addConnectionConfig(connectionConfig, override);
    }

    /**
     * Configure expanded JDBC parameters for opening a connection if needed
     *
      @param driver class name of driver
     * @param url JDBC URL
     * @param props properties with additional parameters a driver can take.
     */
    public void jdbc(String driver, String url, Properties props) {
        ConnectionJdbcConfig connectionConfig = new ConnectionJdbcConfig(driver, url, props);
        connectionConfig.setDbName(dbName);
        connectionConfig.setEnvironment(environment);
        connectionConfig.setTesting(testing);
        DbConfiguration.addConnectionConfig(connectionConfig, override);
    }

    /**
     * Name of a database. If this method is not called, the name od database is presumed "default".
     * @param dbName name od database for ActiveJDBC models.
     * @return self
     */
    public ConnectionBuilder db(String dbName) {
        this.dbName = dbName;
        return this;
    }

    /**
     * Marks this connection to be used for testing. When you use any of the testing classes, such as <code>DBSpec, DBControllerSpec,
     * DBIntegrationSpec, AppIntegrationSpec</code> from <code>activeweb-testing</code> module, they all will use a connection that is marked
     * by this method.
     *
     * @return self
     */
    public ConnectionBuilder testing() {
         this.testing = true;
        return this;
    }

    /**
     * Sets a <code>DataSource</code> to be used by this configuration.
     *
     * @param dataSource instance of a datasource
     */
    public void dataSource(DataSource dataSource) {
        ConnectionDataSourceConfig connectionConfig = new ConnectionDataSourceConfig(dataSource);
        connectionConfig.setDbName(dbName);
        connectionConfig.setEnvironment(environment);
        connectionConfig.setTesting(testing);
        DbConfiguration.addConnectionConfig(connectionConfig, override);
    }
}
