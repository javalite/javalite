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
package activeweb;

import activeweb.freemarker.FreeMarkerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static javalite.common.Util.blank;

/**
 * @author Igor Polevoy
 */
public class Configuration {
    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    enum Params{templateManager, bootstrap, defaultLayout, targetDir, rootPackage, dbconfig, rollback, freemarkerConfigurer}

    private static final Configuration instance = new Configuration();
    private static Properties props = new Properties();
    private static TemplateManager templateManager;    
    private HashMap<String, List<ConnectionSpecWrapper>> connectionWrappers = new HashMap<String, List<ConnectionSpecWrapper>>();
    private boolean testing = false;
    private static String ENV;
    private static boolean activeReload = !blank(System.getProperty("active_reload")) && System.getProperty("active_reload").equals("true");

    static{
        try {
            //read defaults
            props = new Properties();
            InputStream in1 = Configuration.class.getClassLoader().getResourceAsStream("activeweb_defaults.properties");
            props.load(in1);

            //override defaults
            Properties overrides = new Properties();
            InputStream in2 = Configuration.class.getClassLoader().getResourceAsStream("activeweb.properties");
            if(in2 != null){
                overrides.load(in2);
            }

            for (Object name : overrides.keySet()) {
                props.put(name, overrides.get(name));
            }
            checkInitProperties();
            initTemplateManager();            
        }
        catch (Exception e) {
            throw new InitException(e);
        }
    }


    public static boolean logRequestParams() {
        String logRequest = System.getProperty("activeweb.log.request");
        return logRequest != null && logRequest.equals("true");
    }
    
    public String getEnv(){
        if(ENV == null){
            ENV = System.getenv().get("ACTIVE_ENV");
            if(blank(ENV)){                
                ENV = "development";
                logger.warn("Environment variable ACTIVE_ENV not provided, defaulting to '" + ENV + "'");
            }
        }
        return ENV;
    }
    
    public boolean isTesting() {
        return testing;
    }

    protected void setTesting(boolean testing) {
        this.testing = testing;
    }

    public static Configuration instance(){
        return instance;
    }

    private static void checkInitProperties(){
        for(Params param: Params.values()){
            if(props.get(param.toString()) == null){
                throw new InitException("Must provide property: " + param);
            }
        }
    }

    private static void initTemplateManager() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        templateManager = (TemplateManager)Class.forName(get(Params.templateManager.toString())).newInstance();
    }


    public static FreeMarkerConfigurer getFreemarkerConfigurer(){
        try{
            String className = get(Params.freemarkerConfigurer.toString());
            return (FreeMarkerConfigurer)Class.forName(className).newInstance();

        }catch(Exception e){
            throw new InitException("failed to create an instance of FreeMarkerConfigurer...", e);
        }
    }


    public static TemplateManager getTemplateManager(){
        return templateManager;
    }

    public static String get(String name){return props.getProperty(name);}

    public static String getDefaultLayout() {
        return get(Params.defaultLayout.toString());
    }

    public static String getBootstrapClassName() {
        return get(Params.bootstrap.toString());
    }

    public static String getTargetDir() {
        return get(Params.targetDir.toString());  
    }

    public static String getRootPackage() {
        return get(Params.rootPackage.toString());  
    }

    public static boolean rollback() {
        return Boolean.parseBoolean(get(Params.rollback.toString().trim()));  
    }    

    protected void addConnectionWrapper(ConnectionSpecWrapper connectionWrapper) {
        String connectionWrapperEnv = connectionWrapper.getEnvironment();
        List<ConnectionSpecWrapper> envConnectionWrappers = connectionWrappers.get(connectionWrapperEnv);
        if(envConnectionWrappers == null) {
            envConnectionWrappers = new ArrayList<ConnectionSpecWrapper>();
            connectionWrappers.put(connectionWrapperEnv, envConnectionWrappers);
        }
        envConnectionWrappers.add(connectionWrapper);
    }

    public List<ConnectionSpecWrapper> getConnectionWrappers() {
        return getConnectionSpecWrappers(getEnv());
    }

    public List<ConnectionSpecWrapper> getConnectionSpecWrappers(String env) {
        return connectionWrappers.get(env) == null? new ArrayList<ConnectionSpecWrapper>() :connectionWrappers.get(env);
    }

    protected void clearConnectionWrappers() {
        clearConnectionWrappers(getEnv());
    }

    protected void clearConnectionWrappers(String env) {
        if(connectionWrappers.get(env) != null)
            connectionWrappers.get(env).clear();
    }



    public static boolean activeReload(){
        return activeReload;
    }
}
