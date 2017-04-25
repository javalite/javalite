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

import org.javalite.activejdbc.Configuration;
import org.javalite.activejdbc.InitException;
import org.javalite.common.Util;

import java.util.*;

import static java.util.stream.Collectors.toSet;

/**
 * @author igor on 12/2/16.
 */
public class DbConfiguration {

    private static HashMap<String, List<ConnectionSpecWrapper>> connectionWrappers = new HashMap<>();

    private String configFile;

    public static void addConnectionWrapper(ConnectionSpecWrapper connectionWrapper, boolean override) {
        String connectionWrapperEnv = connectionWrapper.getEnvironment();
        List<ConnectionSpecWrapper> envConnectionWrappers = connectionWrappers.get(connectionWrapperEnv);
        if(envConnectionWrappers == null || override) {
            envConnectionWrappers = new ArrayList<>();
            connectionWrappers.put(connectionWrapperEnv, envConnectionWrappers);
        }
        envConnectionWrappers.add(connectionWrapper);
    }

    /**
     * Provides a list of all connection wrappers corresponding to a current environment.
     *
     * @return  a list of all connection wrappers corresponding to a current environment.
     */
    public static List<ConnectionSpecWrapper> getConnectionSpecWrappers() {
        return getConnectionSpecWrappers(Configuration.getEnv());
    }

    /**
     * Provides a list of all connection wrappers corresponding to a given environment.
     *
     * @param env name of environment, such as "development", "production", etc.
     *
     * @return  a list of all connection wrappers corresponding to a given environment.
     */
    public static List<ConnectionSpecWrapper> getConnectionSpecWrappers(String env) {
        return connectionWrappers.get(env) == null? new ArrayList<>() :connectionWrappers.get(env);
    }

    public static void clearConnectionWrappers() {
        clearConnectionWrappers(Configuration.getEnv());
    }


    //for tests only
    public static void resetConnectionWrappers() {
        connectionWrappers = new HashMap<>();
    }


    protected static void clearConnectionWrappers(String env) {
        if(connectionWrappers.get(env) != null)
            connectionWrappers.get(env).clear();
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
    public void loadConfiguration(String file) {

        if(configFile == null)

        try {
            Properties props = Util.readProperties(file);
            Set<String> environments = props.stringPropertyNames().stream().map(n -> n.substring(0, n.lastIndexOf("."))).collect(toSet());
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
        addConnectionWrapper(wrapper, false);
    }

    private void createJndiWrapper(String env, String jndiName) {
        ConnectionSpecWrapper wrapper = new ConnectionSpecWrapper();
        wrapper.setEnvironment(env);
        ConnectionJndiSpec connectionSpec = new ConnectionJndiSpec(jndiName);
        wrapper.setConnectionSpec(connectionSpec);
        addConnectionWrapper(wrapper, false);
    }

    public List<ConnectionSpecWrapper> getTestConnectionWrappers() {
        List<ConnectionSpecWrapper> allConnections = getConnectionSpecWrappers();
        List<ConnectionSpecWrapper> result = new LinkedList<>();

        for (ConnectionSpecWrapper connectionWrapper : allConnections) {
            if (connectionWrapper.isTesting())
                result.add(connectionWrapper);
        }

        return result;
    }
}
