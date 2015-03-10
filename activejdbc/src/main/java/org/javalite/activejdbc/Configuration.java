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
import org.javalite.activejdbc.dialects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private final static Logger logger = LoggerFactory.getLogger(Configuration.class);

    private Map<String, Dialect> dialects = new CaseInsensitiveMap<Dialect>();

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
            }catch(Exception e){
                throw new InitException("failed to initialize a CacheManager. Please, ensure that the property " +
                        "'cache.manager' points to correct class which extends 'activejdbc.cache.CacheManager' class and provides a default constructor.", e);
            }

        }
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