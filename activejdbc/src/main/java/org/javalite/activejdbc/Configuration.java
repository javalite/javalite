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
package org.javalite.activejdbc;

import org.javalite.activejdbc.connection_config.ConnectionJdbcSpec;
import org.javalite.activejdbc.connection_config.ConnectionJndiSpec;
import org.javalite.activejdbc.connection_config.ConnectionSpec;
import org.javalite.activejdbc.logging.ActiveJDBCLogger;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import org.javalite.common.Convert;

import static org.javalite.common.Util.*;

/**
 * @author Igor Polevoy
 */
public class Configuration {

    /**
     * All properties defined in framework.
     */
    public enum PropertyName {
        CacheManager("cache.manager"),
        ActiveJdbcLogger("activejdbc.logger"),
        EnvConnectionsFile("env.connections.file"),
        CollectStatistics("collectStatistics"),
        CollectStatisticsOnHold("collectStatisticsOnHold");

        private String name;

        PropertyName(String name) {
            this.name = name;
        }
    }

    private Properties properties = new Properties();
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static String ENV;
    private static ActiveJDBCLogger activeLogger;

    private Map<String, ConnectionSpec> connectionSpecMap = new HashMap<>();

    protected Configuration(){

        loadProjectProperties();
        loadOverridesFromSystemProperties();
        loadConnectionsSpecs();

        String loggerClass = properties.getProperty(PropertyName.ActiveJdbcLogger.name);
        if(loggerClass != null){
            try {
                activeLogger = (ActiveJDBCLogger) Class.forName(loggerClass).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new InitException("Failed to initialize a ActiveJDBCLogger. Please, ensure that the property " +
                        "'activejdbc.logger' points to correct class which extends '" + ActiveJDBCLogger.class.getName()
                        + "' and provides a default constructor.", e);
            }
        }
    }

    /**
     * A system property, if provided, will override a property from a file <code>activejdbc.properties</code>.
     * That is only if defined in {@link PropertyName}.
     *
     */
    private void loadOverridesFromSystemProperties() {
        PropertyName[] names = PropertyName.values();
        for (PropertyName propertyName: names){
            String sysProp = System.getProperty(propertyName.name);
            if(sysProp != null){
                properties.put(propertyName.name, sysProp);
            }
        }
    }

    private void loadProjectProperties() {
        try {
            InputStream in = getClass().getResourceAsStream("/activejdbc.properties");
            if (in != null) { properties.load(in); }
        } catch (IOException e){
            throw new InitException(e);
        }
    }

    private void loadConnectionsSpecs() {
        try{
            String propertyFileName = properties == null ? "database.properties"
                    : properties.getProperty(PropertyName.EnvConnectionsFile.name, "database.properties");

            Properties connectionProps = readPropertyFile(propertyFileName);
            for (String env : getEnvironments(connectionProps)) {
                String jndiName = env + "." + "jndi";
                if (connectionProps.containsKey(jndiName)) {
                    connectionSpecMap.put(env, new ConnectionJndiSpec(connectionProps.getProperty(jndiName)));
                } else {
                    String driver = connectionProps.getProperty(env + ".driver");
                    String userName = connectionProps.getProperty(env + ".username");
                    String password = connectionProps.getProperty(env + ".password");
                    String url = connectionProps.getProperty(env + ".url");
                    if (driver == null || userName == null || password == null || url == null) {
                        throw new InitException("Four JDBC properties are expected: driver, username, password, url for environment: " + env);
                    }
                    connectionSpecMap.put(env, new ConnectionJdbcSpec(driver, url, userName, password));
                }
            }
        }catch(Exception e){
            // in case property file not found, do nothing
        }

        overrideFromEnvironmentVariables();
        overrideFromSystemProperties();
    }

    /**
     * Overrides current environment's connection spec from system properties.
     */
    private void overrideFromEnvironmentVariables() {
        String  url = System.getenv("ACTIVEJDBC.URL");
        String  user = System.getenv("ACTIVEJDBC.USER");
        String  password = System.getenv("ACTIVEJDBC.PASSWORD");
        String  driver = System.getenv("ACTIVEJDBC.DRIVER");
        if(!blank(url) && !blank(user) && !blank(password) && !blank(driver)){
            connectionSpecMap.put(getEnvironment(), new ConnectionJdbcSpec(driver, url, user, password));
        }

        String  jndi = System.getenv("ACTIVEJDBC.JNDI");
        if(!blank(jndi)){
            connectionSpecMap.put(getEnvironment(), new ConnectionJndiSpec(jndi));
        }
    }

    /**
     * Overrides current environment's connection spec from system properties.
     */
    private void overrideFromSystemProperties() {
        String  url = System.getProperty("activejdbc.url");
        String  user = System.getProperty("activejdbc.user");
        String  password = System.getProperty("activejdbc.password");
        String  driver = System.getProperty("activejdbc.driver");
        if(!blank(url) && !blank(user) && !blank(driver)){
            connectionSpecMap.put(getEnvironment(), new ConnectionJdbcSpec(driver, url, user, password));
        }

        String  jndi = System.getProperty("activejdbc.jndi");
        if(!blank(jndi)){
            connectionSpecMap.put(getEnvironment(), new ConnectionJndiSpec(jndi));
        }
    }

    public ConnectionSpec getConnectionSpec(String environment){
        return connectionSpecMap.get(environment);
    }

    /**
     * Finds a connection {@link ConnectionSpec} that corresponds to system properties,
     * environment variables of <code>database.properties</code> configuration, whichever is found first.
     * Configuration of system properties overrides environment variables, which overrides
     * <code>database.properties</code>.
     *
     * @return {@link ConnectionSpec} used by {@link DB#open()} to open a "default" connection, as well as {@link Base#open()} methods.
     */
    public  ConnectionSpec getCurrentConnectionSpec(){
        return getConnectionSpec(getEnvironment());
    }


    /**
     * @return current environment as specified by environment variable <code>ACTIVE_ENV</code>
     * of <code>active_env</code> system property. System property value overrides environment variable.
     *
     * Defaults to "development" if no environment variable provided.
     */
    public String getEnvironment(){
        String env = "development";

        if(!blank(System.getenv("ACTIVE_ENV"))){
            env = System.getenv("ACTIVE_ENV");
        }

        if(!blank(System.getProperty("active_env"))){
            env = System.getProperty("active_env");
        }
        return env;
    }

    //get environments defined in properties
    private Set<String> getEnvironments(Properties props) {
        Set<String> environments = new HashSet<>();
        for (Object k : props.keySet()) {
            String environment = k.toString().split("\\.")[0];
            environments.add(environment);
        }
        return new TreeSet<>(environments);
    }

    //read from classpath, if not found, read from file system. If not found there, throw exception
    private Properties readPropertyFile(String file) throws IOException {
        String fileName = file.startsWith("/") ? file : "/" + file;
        LOGGER.info("Reading properties from: " + fileName + ". Will try classpath, then file system.");
        return Util.readProperties(fileName);
    }


    public boolean collectStatistics() {
        return Convert.toBoolean(properties.getProperty(PropertyName.CollectStatistics.name, "false"));
    }

    public boolean collectStatisticsOnHold() {
        return Convert.toBoolean(properties.getProperty(PropertyName.CollectStatisticsOnHold.name, "false"));
    }

    public String getCacheManager(){
        return properties.getProperty(Configuration.PropertyName.CacheManager.name);
    }

    /**
     * Returns name of environment, such as "development", "production", etc.
     * This is a value that is usually setup with an environment variable <code>ACTIVE_ENV</code>.
     *
     * @return name of environment
     */
    public static String getEnv(){
        if(ENV == null){
            if(!blank(System.getenv("ACTIVE_ENV"))) {
                ENV = System.getenv("ACTIVE_ENV");
            }

            if(!blank(System.getProperty("ACTIVE_ENV"))) {
                ENV = System.getProperty("ACTIVE_ENV");
            }

            if(!blank(System.getProperty("active_env"))) {
                ENV = System.getProperty("active_env");
            }

            if(blank(ENV)){
                ENV = "development";
                LogFilter.log(LOGGER, LogLevel.INFO, "Environment variable ACTIVE_ENV not provided, defaulting to '" + ENV + "'");
            }
        }
        return ENV;
    }

    /**
     * This method must ony be used in tests. Use in other environments at your own peril.
     *
     * @param env name of environment (development, staging, production, etc.).
     */
    public static void setEnv(String env){
        ENV = env;
    }

    /**
     * @return true if a custom logger is defined, false ther
     */
    public static boolean hasActiveLogger() {
        return activeLogger != null;
    }

    public static ActiveJDBCLogger getActiveLogger() {
        return activeLogger;
    }
}