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

import org.javalite.activejdbc.Configuration;
import org.javalite.activejdbc.InitException;
import org.javalite.app_config.AppConfig;
import org.javalite.common.Util;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.javalite.common.Util.blank;

/**
 * @author igor on 12/2/16.
 */
public class DBConfiguration {

    /**
     * Key is environment, such as 'development', 'production'
     */
    private static HashMap<String, List<ConnectionConfig>> connectionConfigs = new HashMap<>();


    /**
     * Adds a new connection config for an environment.
     *
     * @param connectionConfig connection configuration object
     * @param override true to override a previous config  for the same environment
     */
    public static void addConnectionConfig(ConnectionConfig connectionConfig, boolean override) {

        //TODO: what to do for multiple connections in the same environment??
        String connectionEnv = connectionConfig.getEnvironment();
        List<ConnectionConfig> connectionConfigList = connectionConfigs.get(connectionEnv);
        if(connectionConfigList == null || override) {
            connectionConfigList = new ArrayList<>();
            connectionConfigs.put(connectionEnv, connectionConfigList);
        }
        connectionConfigList.add(connectionConfig);
    }

    /**
     * Provides a list of all connection configs corresponding to a current environment.
     *
     * @return  a list of all connection configs corresponding to a current environment.
     */
    public static List<ConnectionConfig> getConnectionConfigs() {
        return getConnectionConfigs(Configuration.getEnv());
    }

    /**
     * Provides a list of all connection configs corresponding to a given environment.
     *
     * @param env name of environment, such as "development", "production", etc.
     *
     * @return  a list of all connection configs corresponding to a given environment.
     */
    public static List<ConnectionConfig> getConnectionConfigs(String env) {
        return connectionConfigs.get(env) == null? new ArrayList<>() : connectionConfigs.get(env);
    }



    /**
     * Returns all connections which correspond dbName and current environment
     *
     * @return all connections which correspond dbName and current environment
     */
    public static List<ConnectionConfig> getConnectionConfigsExceptTesting(String dbName) {

        if(dbName ==  null) {
            throw new IllegalArgumentException("dbName cannot be null");
        }

        List<ConnectionConfig> allConnections = getConnectionConfigs();
        List<ConnectionConfig> result = new LinkedList<>();

        for (ConnectionConfig connectionConfig : allConnections) {
            if (!connectionConfig.isTesting() && dbName.equals(connectionConfig.getDbName()))
                result.add(connectionConfig);
        }
        return result;
    }


    public static void clearConnectionConfigs() {
        clearConnectionConfigs(Configuration.getEnv());
    }


    //for tests only
    public static void resetConnectionConfigs() {
        connectionConfigs = new HashMap<>();
    }


    protected static void clearConnectionConfigs(String env) {
        if(connectionConfigs.get(env) != null)
            connectionConfigs.get(env).clear();
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

     # this one is to run migrations in production remotely
     production.remote.driver=com.mysql.jdbc.Driver
     production.remote.username=root
     production.remote.password=xxx
     production.remote.url=jdbc:mysql://127.0.0.1:3307/poj1_production

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
     *     <li>All connections specified in a property file automatically assigned DB name "default"
     * </ul>
     *
     * @param file path to a file. Can be located on classpath, or on a file system. First searched on classpath,
     *             then on file system.
     */
    public static void loadConfiguration(String file) {

        /*
         *  TODO:  it needs to be documented that in order for https://github.com/javalite/javalite/issues/469 to work, this
         *   method should be called explicitly from a non-ActiveWeb app to pickup configuration from the property file
         *   as well as overrides from system properties as well as from environment variables
         */

        try {
            Properties props = Util.readProperties(file);
            Set<String> environments = props.stringPropertyNames().stream().map(n -> n.substring(0, n.lastIndexOf("."))).collect(toSet());
            for (String env : environments) {
                String jndiName = env + "." + "jndi";
                if (props.containsKey(jndiName)) {
                    createJndiConfig(env, props.getProperty(jndiName));
                } else {
                    String driver = props.getProperty(env + ".driver");
                    String userName = props.getProperty(env + ".username");
                    String password = props.getProperty(env + ".password");
                    String url = props.getProperty(env + ".url");
                    checkProps(driver, userName, password, url, env);
                    createJdbcConfig(env, driver, url, userName, password);
                }
            }
        } catch (InitException e) {
            throw e;
        } catch (Exception e) {
            throw new InitException(e);
        }

        overrideFromEnvironmentVariables();
        overrideFromSystemProperties();
    }

    private static void checkProps(String driver, String userName, String password, String url, String env){
        if (driver == null || userName == null || password == null || url == null){
            throw new InitException("Four JDBC properties are expected: driver, username, password, url for environment: " + env);
        }
    }

    private static void createJdbcConfig(String env, String driver, String url, String userName, String password) {

        ConnectionJdbcConfig connectionJdbcConfig = new ConnectionJdbcConfig(driver, url, userName, password);
        connectionJdbcConfig.setEnvironment(env);


        if(env.equals("test")){
            connectionJdbcConfig.setEnvironment("development");
            connectionJdbcConfig.setTesting(true);
        } else if(env.endsWith(".test")) {
            connectionJdbcConfig.setEnvironment(env.split("\\.")[0]);
            connectionJdbcConfig.setTesting(true);
        }else{
            connectionJdbcConfig.setEnvironment(env);
        }

        addConnectionConfig(connectionJdbcConfig, false);
    }

    private static void createJndiConfig(String env, String jndiName) {
        ConnectionJndiConfig connectionJndiConfig = new ConnectionJndiConfig(jndiName);
        connectionJndiConfig.setEnvironment(env);
        addConnectionConfig(connectionJndiConfig, false);
    }

    /**
     * @return list of {@link ConnectionConfig} objects that are marked for testing.
     */
    public List<ConnectionConfig> getTestConnectionConfigs() {
        return getConnectionConfigs().stream().filter(ConnectionConfig::isTesting).collect(Collectors.toList());
    }


    /**
     * Overrides current environment's connection spec from system properties.
     */
    private static void overrideFromEnvironmentVariables() {
        String  url = System.getenv("ACTIVEJDBC.URL");
        String  user = System.getenv("ACTIVEJDBC.USER");
        String  password = System.getenv("ACTIVEJDBC.PASSWORD");
        String  driver = System.getenv("ACTIVEJDBC.DRIVER");
        if(!blank(url) && !blank(user) && !blank(password) && !blank(driver)){

            ConnectionJdbcConfig jdbcConfig = new ConnectionJdbcConfig(driver, url, user, password);
            jdbcConfig.setEnvironment(AppConfig.activeEnv());
            addConnectionConfig(jdbcConfig, true);
        }

        String  jndi = System.getenv("ACTIVEJDBC.JNDI");
        if(!blank(jndi)){
            ConnectionJndiConfig jndiConfig = new ConnectionJndiConfig(jndi);
            jndiConfig.setEnvironment(AppConfig.activeEnv());
            addConnectionConfig(jndiConfig, true);
        }
    }

    /**
     * Overrides current environment's connection spec from system properties.
     */
    private static void overrideFromSystemProperties() {
        String  url = System.getProperty("activejdbc.url");
        String  user = System.getProperty("activejdbc.user");
        String  password = System.getProperty("activejdbc.password");
        String  driver = System.getProperty("activejdbc.driver");
        if(!blank(url) && !blank(user) && !blank(driver)){
            ConnectionJdbcConfig jdbcConfig = new ConnectionJdbcConfig(driver, url, user, password);
            jdbcConfig.setEnvironment(AppConfig.activeEnv());
            addConnectionConfig(jdbcConfig, true);
        }

        String  jndi = System.getProperty("activejdbc.jndi");
        if(!blank(jndi)){
            ConnectionJndiConfig jndiConfig = new ConnectionJndiConfig(jndi);
            jndiConfig.setEnvironment(AppConfig.activeEnv());
            addConnectionConfig(jndiConfig, true);
        }
    }

}
