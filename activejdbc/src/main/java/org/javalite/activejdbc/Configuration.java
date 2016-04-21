/*
Copyright 2009-2015 Igor Polevoy

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

import org.javalite.activejdbc.cache.CacheManager;
import org.javalite.activejdbc.cache.NopeCacheManager;
import org.javalite.activejdbc.dialects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import org.javalite.common.Convert;

import static org.javalite.common.Util.*;

/**
 * @author Igor Polevoy
 */
public class Configuration {

    //key is a DB name, value is a list of model names
    private Map<String, List<String>> modelsMap = new HashMap<String, List<String>>();
    private Properties properties = new Properties();
    private static CacheManager cacheManager;
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private Map<String, Dialect> dialects = new CaseInsensitiveMap<>();

    private Map<String, ConnectionSpec> connectionSpecMap = new HashMap<>();

    protected Configuration(){
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("activejdbc_models.properties");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                LogFilter.log(logger, "Load models from: {}", url.toExternalForm());
                InputStream inputStream = null;
                InputStreamReader isreader = null;
                BufferedReader reader = null;
                try {
                    inputStream = url.openStream();
                    isreader = new InputStreamReader(inputStream);
                    reader = new BufferedReader(isreader);
                    String line;
                    while ((line = reader.readLine()) != null) {

                        String[] parts = split(line, ':');
                        String modelName = parts[0];
                        String dbName = parts[1];

                        List<String> modelNames = modelsMap.get(dbName);
                        if (modelNames == null) {
                            modelNames = new ArrayList<String>();
                            modelsMap.put(dbName, modelNames);
                        }
                        modelNames.add(modelName);
                    }
                } finally {
                    closeQuietly(reader);
                    closeQuietly(isreader);
                    closeQuietly(inputStream);
                }
            }
        } catch (IOException e) {
            throw new InitException(e);
        }
        if(modelsMap.isEmpty()){
            LogFilter.log(logger, "ActiveJDBC Warning: Cannot locate any models, assuming project without models.");
            return;
        }
        try {
            InputStream in = getClass().getResourceAsStream("/activejdbc.properties");
            if (in != null) { properties.load(in); }
        } catch (IOException e){
            throw new InitException(e);
        }

        String cacheManagerClass = properties.getProperty("cache.manager");
        if(cacheManagerClass != null){
            try{
                Class cmc = Class.forName(cacheManagerClass);
                cacheManager = (CacheManager)cmc.newInstance();
            }catch(InitException e){
                throw e;
            }catch(Exception e){
                throw new InitException("failed to initialize a CacheManager. Please, ensure that the property " +
                        "'cache.manager' points to correct class which extends '" + CacheManager.class.getName() + "' and provides a default constructor.", e);
            }
        }else{
            cacheManager = new NopeCacheManager();
        }
        loadConnectionsSpecs();
    }

    private void loadConnectionsSpecs() {
        try{

            String propertyFileName = properties == null ? "database.properties"
                    : properties.getProperty("env.connections.file", "database.properties");

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
        if(!blank(url) && !blank(user) && !blank(password) && !blank(driver)){
            connectionSpecMap.put(getEnvironment(), new ConnectionJdbcSpec(driver, url, user, password));
        }

        String  jndi = System.getenv("activejdbc.jndi");
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
     * @return current environment as specified by environment variable <code>ACTIVE_ENV</code>.
     * Defaults to "development" if no environment variable provided.
     */
    public String getEnvironment(){
        return System.getenv("ACTIVE_ENV")  == null ? "development" : System.getenv("ACTIVE_ENV");
    }

    //get environments defined in properties
    private Set<String> getEnvironments(Properties props) {
        Set<String> environments = new HashSet<String>();
        for (Object k : props.keySet()) {
            String environment = k.toString().split("\\.")[0];
            environments.add(environment);
        }
        return new TreeSet<String>(environments);
    }

    //read from classpath, if not found, read from file system. If not found there, throw exception
    private Properties readPropertyFile(String file) throws IOException {

        String fileName = file.startsWith("/") ? file : "/" + file;
        InputStream in = getClass().getResourceAsStream(fileName);
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


    List<String> getModelNames(String dbName) throws IOException {
        return modelsMap.get(dbName);
    }

    public boolean collectStatistics() {
        return Convert.toBoolean(properties.getProperty("collectStatistics", "false"));
    }

    public boolean collectStatisticsOnHold() {
        return Convert.toBoolean(properties.getProperty("collectStatisticsOnHold", "false"));
    }

    public boolean cacheEnabled(){
        return cacheManager != null;
    }

    Dialect getDialect(MetaModel mm){
        Dialect dialect = dialects.get(mm.getDbType());
        if (dialect == null) {
            if(mm.getDbType().equalsIgnoreCase("Oracle")){
                dialect = new OracleDialect();
            }
            else if(mm.getDbType().equalsIgnoreCase("MySQL")){
                dialect = new MySQLDialect();
            }
            else if(mm.getDbType().equalsIgnoreCase("PostgreSQL")){
                dialect = new PostgreSQLDialect();
            }
            else if(mm.getDbType().equalsIgnoreCase("h2")){
                dialect = new H2Dialect();
            }
            else if(mm.getDbType().equalsIgnoreCase("Microsoft SQL Server")){
                dialect = new MSSQLDialect();
            }
            else if(mm.getDbType().equalsIgnoreCase("SQLite")){
                dialect = new SQLiteDialect();
            }else{
                dialect = new DefaultDialect();
            }
            dialects.put(mm.getDbType(), dialect);
        }
        return dialect;
    }


    public CacheManager getCacheManager(){
        return cacheManager;
    }
}