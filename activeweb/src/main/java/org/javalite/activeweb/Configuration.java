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
package org.javalite.activeweb;

import com.google.inject.Injector;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.activeweb.freemarker.AbstractFreeMarkerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import java.io.File;
import java.io.InputStream;
import java.util.*;

import static org.javalite.common.Util.blank;

/**
 * @author Igor Polevoy
 */
public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static Injector injector;

    static List<String> getControllerPackages() {
        return controllerPackages;
    }

    enum Params {
        templateManager, bootstrap, defaultLayout, targetDir, rootPackage, dbconfig, controllerConfig, rollback,
        freeMarkerConfig, route_config, maxUploadSize
    }

    private static Properties props = new Properties();
    private static TemplateManager templateManager;

    private static boolean testing;
    private static String ENV;
    private static boolean activeReload = !blank(System.getProperty("active_reload")) && System.getProperty("active_reload").equals("true");
    private static AbstractFreeMarkerConfig freeMarkerConfig;
    private static boolean useDefaultLayoutForErrors = true;

    // these are not full package names, just parti al package names between "app.controllers"
    // and simple name of controller class
    private static List<String> controllerPackages;
    private static List<HttpSupportFilter> filters  = new ArrayList<>();

    private static boolean filtersInjected = false;

    private static Map<HttpSupport, FilterMetadata> filterMetadataMap = new HashMap<>();

    static{
        try {
            //read defaults
            props = new Properties();
            InputStream in1 = Configuration.class.getClassLoader().getResourceAsStream("activeweb_defaults.properties");
            props.load(in1);

            //override defaults
            Properties overrides = new Properties();
            InputStream in2 = Configuration.class.getResourceAsStream("/activeweb.properties");
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
     * False will ensure that these pages will render without default layout.
     *
     * <h2>System errors</h2>
     * The following system errors are affected by this setting:
     *
     * <ul>
     *     <li>CompilationException - when there are compilation errors in controller</li>
     *     <li>ClassLoadException - failure to load a controller class for any reason </li>
     *     <li>ActionNotFoundException - action method is not found in controller class</li>
     *     <li>ViewMissingException - corresponding view is missing</li>
     *     <li>ViewException - FreeMarker barfed on the view</li>
     * </ul>
     *
     * If you need custom dynamic layout for error.ftl and 404.ftl, use "@wrap" tag and conditions inside the error templates.
     *
     * <h2>Application level</h2>
     * This method does <em>not</em> affect application errors (exceptions thrown by your code).
     * However, it is typical for an ActiveWeb project to define a top controller filter called CatchAllFilter and process
     * application level exceptions in that filter:
     * <pre>
            package app.controllers;
            import org.javalite.activeweb.controller_filters.HttpSupportFilter;
            import static org.javalite.common.Collections.map;
            public class CatchAllFilter extends HttpSupportFilter {
                public void onException(Exception e) {
                    render("/system/error", map("message", e.getMessage())).layout("error_layout");
                }
            }
     * </pre>
     *
     * This way you have a complete control over how your error messages are displayed.
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


    /**
     * Returns true if the environment is the same as argument.
     *
     * @param environment name of environment in question.
     * @return true if argument equals value of environment variable <code>ACTIVE_ENV</code>, false if not.
     */
    public static boolean runningIn(String environment){
        return getEnv().equals(environment);
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
                LOGGER.warn("Environment variable ACTIVE_ENV not provided, defaulting to '" + ENV + "'");
            }
        }
        return ENV;
    }

    //only for testing!
    protected static void setEnv(String env){
        ENV = env;
    }

    /**
     * This method is used internally by ActiveWeb tests. Do not use in  your projects.
     * If you need this feature, use:
     * <a href="http://javalite.github.io/activejdbc/snapshot/org/javalite/app_config/AppConfig.html#isInTestMode--">AppConfig#isInTestMode</a>
     *
     * @return true  if running in tests.
     */
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
            LOGGER.debug("Failed to find implementation of '" + AbstractFreeMarkerConfig.class + "', proceeding without custom configuration of FreeMarker");
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

    public static boolean activeReload(){
        return activeReload;
    }

    public static int getMaxUploadSize() {
        return Integer.parseInt(get(Params.maxUploadSize.toString()));
    }

    public static File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    protected static void setFilters(List<HttpSupportFilter> allFilters) {
        filters = allFilters;
    }

    protected static void setFilterConfig(FilterConfig config) {
        controllerPackages = ControllerPackageLocator.locateControllerPackages(config);
    }

    protected static List<HttpSupportFilter> getFilters() {
        return filters;
    }

    //does not have to be synchronized. In the worst case, filters will be injected
    // the same stuff a few times.
    protected static void injectFilters() {
        if(injector != null ){
            if(Configuration.isTesting()){
                for (HttpSupportFilter filter : filters) {
                    injector.injectMembers(filter);
                }
            } else if (!filtersInjected) {
                for (HttpSupportFilter filter : filters) {
                    injector.injectMembers(filter);
                }
                filtersInjected = true;
            }
        }
    }

    //no need to synchronize here, since FilterConfig objects will be created
    //by a single thread when the app is bootstrapped.
    static FilterMetadata getFilterMetadata(HttpSupportFilter filter){
        FilterMetadata config = filterMetadataMap.get(filter);
        if(config == null){
            config = new FilterMetadata();
            filterMetadataMap.put(filter, config);
        }
        return config;
    }

    static void setInjector(Injector injector) {
        Configuration.injector = injector;
    }

    static Injector getInjector() {
        return injector;
    }

    static void addFilter(HttpSupportFilter filter) {
        filters.add(filter);
    }

    static void resetFilters() {
        filters = new ArrayList<>();
        filterMetadataMap = new HashMap<>();
    }

}
