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

import org.javalite.activejdbc.logging.ActiveJDBCLogger;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.javalite.common.Convert;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.javalite.common.Util.blank;

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

    protected Configuration(){

        loadProjectProperties();
        loadOverridesFromSystemProperties();

        String loggerClass = properties.getProperty(PropertyName.ActiveJdbcLogger.name);
        if(loggerClass != null){
            try {
                activeLogger = (ActiveJDBCLogger) Class.forName(loggerClass).getDeclaredConstructor().newInstance();
            } catch (InstantiationException
                | IllegalAccessException
                | ClassNotFoundException
                | InvocationTargetException
                | NoSuchMethodException e
            ) {
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

    /**
     * Loads default properties from <code>activejdbc.properties</code>.
     */
    private void loadProjectProperties() {
        try {
            InputStream in = getClass().getResourceAsStream("/activejdbc.properties");
            if (in != null) { properties.load(in); }
        } catch (IOException e){
            throw new InitException(e);
        }
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