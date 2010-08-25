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


package activejdbc;

import activejdbc.cache.CacheManager;
import activejdbc.cache.OSCacheManager;
import activejdbc.dialects.DefaultDialect;
import activejdbc.dialects.MySQLDialect;
import activejdbc.dialects.OracleDialect;
import activejdbc.dialects.PostgreSQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Igor Polevoy
 */
public class Configuration {

    private Properties modelsIndex = new Properties();
    private Properties properties = new Properties();
    final static Logger logger = LoggerFactory.getLogger(Configuration.class);
    
    private  Map<String, DefaultDialect> dialects = new HashMap<String, DefaultDialect>();

    protected Configuration(){
        InputStream modelsIn = getClass().getResourceAsStream("/activejdbc_models.properties");
        if(modelsIn == null){
            LogFilter.log(logger, "ActiveJDBC Warning: Cannot locate any models, assuming project without models.");
            return;
        }
        InputStream in = getClass().getResourceAsStream("/activejdbc.properties");
        try{
            modelsIndex.load(modelsIn);
            if(in != null)
                properties.load(in);
        }
        catch(Exception e){
            throw new InitException(e);
        }
    }
    
    String[] getModelNames() throws IOException {

        List<String> models = new ArrayList<String>();

        for(int i = 0;true ;i++){
            String model = modelsIndex.getProperty("activejdbc.model." + i);
            if(model != null){
                models.add(model);
            }
            else{
                break;
            }
        }
        return models.toArray(new String[0]);
    }


    public boolean collectStatistics(){        
        return properties.getProperty("collectStatistics", "false").equals("true");
    }

    public boolean cacheEnabled(){        
        return properties.getProperty("cache.enabled", "false").equals("true");
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
            else{
                dialects.put(mm.getDbType(), new DefaultDialect());
            }
        }

        return dialects.get(mm.getDbType());
    }

    public CacheManager getCacheManager(){
        //this is a place of extension if a different cache mechanism is needed :
        //http://java-source.net/open-source/cache-solutions
        
        return new OSCacheManager();
    }
}
