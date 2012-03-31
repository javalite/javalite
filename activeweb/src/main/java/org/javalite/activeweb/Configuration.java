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
package org.javalite.activeweb;

import org.javalite.activeweb.freemarker.AbstractFreeMarkerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Condition;

import static org.javalite.common.Util.blank;

/**
 * @author Igor Polevoy
 */
public class Configuration {
    private static Logger LOGGER = LoggerFactory.getLogger(Configuration.class.getName());



    enum Params{templateManager, bootstrap, defaultLayout, targetDir, rootPackage, dbconfig, controllerConfig, rollback, freeMarkerConfig, route_config}

    private static final Configuration instance = new Configuration();
    private static Properties props = new Properties();
    private static TemplateManager templateManager;
    private static HashMap<String, List<ConnectionSpecWrapper>> connectionWrappers = new HashMap<String, List<ConnectionSpecWrapper>>();
    private static boolean testing = false;
    private static String ENV;
    private static boolean activeReload = !blank(System.getProperty("active_reload")) && System.getProperty("active_reload").equals("true");
    private static AbstractFreeMarkerConfig freeMarkerConfig;
    private static boolean useDefaultLayoutForErrors = true;

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

    /**
     * Set to true if you want ActiveWeb to wrap the errors, such as 404, and 500 in a default layout.
     * False will ensure that these pages will render without default layout. If you need custom dynamic layout
     * wrapping these pages, use "@wrap" tag and conditions.
     *
     * @param useDefaultLayoutForErrors true to use default layout, false no not to use it.
     */
    public static void setUseDefaultLayoutForErrors(boolean useDefaultLayoutForErrors) {
        Configuration.useDefaultLayoutForErrors = useDefaultLayoutForErrors;
    }

    /**
     * True to use default layout for error pages, false not to.
     * @return true to use default layout for error pages, false not to.
     */
    protected static boolean useDefaultLayoutForErrors() {
        return useDefaultLayoutForErrors;
    }

    public static boolean logRequestParams() {
        String logRequest = System.getProperty("activeweb.log.request");
        return logRequest != null && logRequest.equals("true");
    }
    
    public static String getEnv(){
        if(ENV == null){
            ENV = System.getenv().get("ACTIVE_ENV");
            if(blank(ENV)){                
                ENV = "development";
                LOGGER.warn("Environment variable ACTIVE_ENV not provided, defaulting to '" + ENV + "'");
            }
        }
        return ENV;
    }
    
    public static boolean isTesting() {
        return testing;
    }

    protected static void setTesting(boolean testing) {
        Configuration.testing = testing;
    }


    private static void checkInitProperties(){
        for(Params param: Params.values()){
            if(props.get(param.toString()) == null){
                throw new InitException("Must provide property: " + param);
            }
        }
    }

    private static void initTemplateManager() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        try{
         templateManager = (TemplateManager)Class.forName(get(Params.templateManager.toString())).newInstance();
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InitException(e);
        }
    }


    public synchronized static AbstractFreeMarkerConfig getFreeMarkerConfig(){
        if(freeMarkerConfig != null) return freeMarkerConfig;

        try{
            String className = get(Params.freeMarkerConfig.toString());
            return freeMarkerConfig = (AbstractFreeMarkerConfig)Class.forName(className).newInstance();
        }catch(Exception e){
            LOGGER.warn("Failed to find implementation of '" + AbstractFreeMarkerConfig.class + "', proceeding without custom configuration of FreeMarker");
            return null;
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

    public static String getControllerConfigClassName(){
        return get(Params.controllerConfig.toString());
    }

    public static String getDbConfigClassName(){
        return get(Params.dbconfig.toString());
    }

    public static String getRouteConfigClassName(){
        return get(Params.route_config.toString());
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

    protected static void addConnectionWrapper(ConnectionSpecWrapper connectionWrapper) {
        String connectionWrapperEnv = connectionWrapper.getEnvironment();
        List<ConnectionSpecWrapper> envConnectionWrappers = connectionWrappers.get(connectionWrapperEnv);
        if(envConnectionWrappers == null) {
            envConnectionWrappers = new ArrayList<ConnectionSpecWrapper>();
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
        return getConnectionSpecWrappers(getEnv());
    }

    /**
     * Provides a list of all connection wrappers corresponding to a given environment.
     *
     * @param env name of environment, such as "development", "production", etc.
     * 
     * @return  a list of all connection wrappers corresponding to a given environment.
     */
    public static List<ConnectionSpecWrapper> getConnectionSpecWrappers(String env) {
        return connectionWrappers.get(env) == null? new ArrayList<ConnectionSpecWrapper>() :connectionWrappers.get(env);
    }

    protected static void clearConnectionWrappers() {
        clearConnectionWrappers(getEnv());
    }

    protected static void clearConnectionWrappers(String env) {
        if(connectionWrappers.get(env) != null)
            connectionWrappers.get(env).clear();
    }

    public static boolean activeReload(){
        return activeReload;
    }
}
