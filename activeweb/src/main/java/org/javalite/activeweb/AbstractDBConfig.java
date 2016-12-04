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


import org.javalite.activejdbc.connection_config.ConnectionJdbcSpec;
import org.javalite.activejdbc.connection_config.ConnectionJndiSpec;
import org.javalite.activejdbc.connection_config.ConnectionSpecWrapper;
import org.javalite.activejdbc.connection_config.DbConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This class is designed to be sub-classed by an application level class called <code>app.config.DbConfig</code>.
 * It is used to configure database connections for various <strong>environments and modes</strong>.
 * <p/>
 * <h4> What is an environment?</h4>
 * An ActiveWeb environment is a computer where a  project executes. In the process of software development there can be
 * a number of environments where a project gets executed, such as development, continuous integration, QA, staging, production and more.
 * The number of environments for ActiveWeb is custom for every project.
 * <p/>
 * <h4>How to specify an environment</h4>
 * An environment is specified by an environment variable <strong>ACTIVE_ENV</strong>. Every computer where an ActiveWeb project
 * gets executed, needs to have this variable specified. This value is used to determine which DB connections need
 * to be initialized.
 * <p/>
 * <h4>Default environment</h4>
 * In case an environment variable <code>ACTIVE_ENV</code> is not provided, the framework defaults to "development".
 * <p/>
 * <h4>What is a mode?</h4>
 * ActiveWeb defines two modes of operation: "standard", which is also implicit, and "testing". Standard mode
 * is used during regular run of the program, and testing used during the build when tests are executed.
 * ActiveWeb promotes a style of development where one database used for testing, but a different one used under normal execution.
 * When tests are executed, a "test" database is used, and when a project is run in a normal mode, a "development"
 * database is used. Having a separate database for testing ensures safety of data in the development database.
 * <p/>
 * <h4> Example of configuration</h4>
 * <pre>
 * 1. public class DbConfig extends AbstractDBConfig {
 * 2.  public void init() {
 * 3.      environment("development").jndi("jdbc/kitchensink_development");
 * 4.      environment("development").testing().jdbc("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/kitchensink_test", "root", "****");
 * 5.      environment("hudson").testing().jdbc("com.mysql.jdbc.Driver", "jdbc:mysql://172.30.64.31/kitchensink_test", "root", "****");
 * 6.      environment("production").jndi("jdbc/kitchensink_production");
 * 7.  }
 * 8.}
 * </pre>
 * The code above is an example from Kitchensink project. Lets examine it line by line.
 * <p/>
 * <ul>
 * <li>Line 3: here we provide configuration for a "standard" mode in "development" environment. This DB connection
 * will be used when the application is running under normal conditions in development environment.
 * <li>Line 4: This is a configuration of DB connection for "development" environment, but for "testing" mode. This
 * connection will be used by unit and integration tests during the build.
 * <li>Line 5: This is a configuration of DB connection for "hudson" environment, but for "testing" mode. The "hudson"
 * environment is a computer where this project is built by Hudson - the continuous integration server. Since Hudson
 * computer is fully automated, and this project is not running there in "standard" mode, there is no standard configuration
 * for Jenkins environment, just one for testing.
 * <li>Line 6: This is configuration similar to one on line 3, but for "production" environment.
 * </ul>
 *
 * @author Igor Polevoy
 */
public abstract class AbstractDBConfig extends AppConfig {

    /**
     * @param environment name of environment (corresponds to env var ACTIVE_ENV)
     * @return builder instance
     */
    public ConnectionBuilder environment(String environment) {
        return new ConnectionBuilder(environment);
    }

    /**
     * @param environment name of environment (corresponds to env var ACTIVE_ENV)
     * @param override true to override any previous configuration for the same environment.
     * @return builder instance
     */
    public ConnectionBuilder environment(String environment, boolean override) {
        return new ConnectionBuilder(environment, override);
    }

    /**
     * Configures multiple database connections from a single property file. Example content for such file:
     *
     * <pre>
     development.driver=com.mysql.jdbc.Driver
     development.username=john
     development.password=pwd
     development.url=jdbc:mysql://localhost/proj_dev

     test.driver=com.mysql.jdbc.Driver
     test.username=mary
     test.password=pwd1
     test.url=jdbc:mysql://localhost/test

     production.jndi=java:comp/env/jdbc/prod
     * </pre>
     *
     * Rules and limitations of using a file-based configuration:
     *
     * <ul>
     *     <li>Only one database connection can be configured per environment (with the exception of development and test connections
     *     only in development environment)</li>
     *     <li>Currently additional database parameters need to be specified as a part of a database URL</li>
     *     <li>Database connection named "test" in the database configuration file is for "development" environment and is
     *     automatically marked for testing (will be used during tests)</li>
     *     <li>If this method of configuration is too limiting, it is possible to mix and match both configuration
     *     methods - file-based, as well as DSL: {@link #environment(String)}</li>
     *     <li>All connections specified in a property file automatically assigned DB name "default". For more on
     *     database names see <a href="http://javalite.io/database_configuration">Database configuration</a></li>
     * </ul>
     *
     * @param file path to a file. Can be located on classpath, or on a file system. First searched on classpath,
     *             then on file system.
     */
    public void configFile(String file) {

        try {
            Properties props = readPropertyFile(file);
            Set<String> environments = getEnvironments(props);
            for (String env : environments) {
                String jndiName = env + "." + "jndi";
                if (props.containsKey(jndiName)) {
                    createJndiWrapper(env, props.getProperty(jndiName));
                } else {
                    String driver = props.getProperty(env + ".driver");
                    String userName = props.getProperty(env + ".username");
                    String password = props.getProperty(env + ".password");
                    String url = props.getProperty(env + ".url");
                    checkProps(driver, userName, password, url, env);
                    createJdbcWrapper(env, driver, url, userName, password);
                }
            }
        } catch (InitException e) {
            throw e;
        } catch (Exception e) {
            throw new InitException(e);
        }
    }

    private void checkProps(String driver, String userName, String password, String url, String env){
        if (driver == null || userName == null || password == null || url == null){
            throw new InitException("Four JDBC properties are expected: driver, username, password, url for environment: " + env);
        }
    }

    private void createJdbcWrapper(String env, String driver, String url, String userName, String password) {
        ConnectionSpecWrapper wrapper = new ConnectionSpecWrapper();
        if(env.equals("test")){
            wrapper.setEnvironment("development");
            wrapper.setTesting(true);
        } else if(env.endsWith(".test")) {
            wrapper.setEnvironment(env.split("\\.")[0]);
            wrapper.setTesting(true);
        }else{
            wrapper.setEnvironment(env);
        }
        ConnectionJdbcSpec connectionSpec = new ConnectionJdbcSpec(driver, url, userName, password);
        wrapper.setConnectionSpec(connectionSpec);
        DbConfiguration.addConnectionWrapper(wrapper, false);
    }

    private void createJndiWrapper(String env, String jndiName) {
        ConnectionSpecWrapper wrapper = new ConnectionSpecWrapper();
        wrapper.setEnvironment(env);
        ConnectionJndiSpec connectionSpec = new ConnectionJndiSpec(jndiName);
        wrapper.setConnectionSpec(connectionSpec);
        DbConfiguration.addConnectionWrapper(wrapper, false);
    }


    private Set<String> getEnvironments(Properties props) {
        Set<String> environments = new HashSet<>();
        for (String prop : props.stringPropertyNames()) {
            String[] parts =  prop.split("\\.");
            if(parts.length == 2){
                environments.add(parts[0]);
            }else if(parts.length == 3 && parts[1].equals("test")){
                environments.add(parts[0] + ".test");
            }else {
                throw new ConfigurationException("Incorrect property: " + prop);
            }
        }
        return environments;
    }

    private Properties readPropertyFile(String file) throws IOException {
        InputStream in = getClass().getResourceAsStream(file);
        Properties props = new Properties();
        if (in != null) {
            props.load(in);
        } else {
            FileInputStream fin = new FileInputStream(file);
            props.load(fin);
            fin.close();
        }
        return props;
    }
}
