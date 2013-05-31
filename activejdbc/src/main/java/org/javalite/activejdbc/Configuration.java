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


package org.javalite.activejdbc;

import org.javalite.activejdbc.cache.CacheManager;
import org.javalite.activejdbc.dialects.*;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * @author Igor Polevoy
 */
public class Configuration {

    //key is a DB name, value is a list of model names
    private Map<String, List<String>> modelsMap = new HashMap<String, List<String>>();
    private Properties properties = new Properties();
    private static CacheManager cacheManager;
    final static Logger logger = LoggerFactory.getLogger(Configuration.class);

    private  Map<String, DefaultDialect> dialects = new HashMap<String, DefaultDialect>();

    protected Configuration(){
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("activejdbc_models.properties");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                LogFilter.log(logger, "Load models from: " + url.toExternalForm());
                InputStream inputStream = null;
                try {
                    inputStream = url.openStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line ;
                    while(  (line = reader.readLine()) != null ){

                        String[] parts = Util.split(line, ':');
                        String modelName = parts[0];
                        String dbName = parts[1];

                        if(modelsMap.get(dbName) == null){
                            modelsMap.put(dbName, new ArrayList<String>());
                        }

                        modelsMap.get(dbName).add(modelName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if( inputStream != null )inputStream.close();
                }
            }
        } catch (IOException e) {
            throw new InitException(e);
        }
        if(modelsMap.isEmpty()){
            LogFilter.log(logger, "ActiveJDBC Warning: Cannot locate any models, assuming project without models.");
            return;
        }
        try{
            InputStream in = getClass().getResourceAsStream("/activejdbc.properties");
            if( in != null ) properties.load(in);
        }
        catch(Exception e){
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
        return properties.getProperty("collectStatistics", "false").equals("true");
    }

    public boolean collectStatisticsOnHold() {
        return properties.getProperty("collectStatisticsOnHold", "false").equals("true");
    }

    public boolean cacheEnabled(){
        return cacheManager != null;
    }

    DefaultDialect getDialect(MetaModel mm){
        if(dialects.get(mm.getDbType()) == null){
            if(mm.getDbType().equalsIgnoreCase("Oracle")){
                dialects.put(mm.getDbType(), new OracleDialect());
            }
            else if(mm.getDbType().equalsIgnoreCase("MySQL")){
                dialects.put(mm.getDbType(), new MySQLDialect());
            }
            else if(mm.getDbType().equalsIgnoreCase("PostgreSQL")){
                dialects.put(mm.getDbType(), new PostgreSQLDialect());
            }
            else if(mm.getDbType().equalsIgnoreCase("h2")){
                dialects.put(mm.getDbType(), new H2Dialect());
            }
            else if(mm.getDbType().equalsIgnoreCase("Microsoft SQL Server")){
                dialects.put(mm.getDbType(), new MSSQLDialect());
            }
            else{
                dialects.put(mm.getDbType(), new DefaultDialect());
            }
        }

        return dialects.get(mm.getDbType());
    }


    public CacheManager getCacheManager(){
        return cacheManager;
    }
}