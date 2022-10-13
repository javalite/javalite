/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.javalite.activeweb.freemarker.AbstractFreeMarkerConfig;
import org.javalite.activeweb.websockets.AbstractWebSocketConfig;
import org.javalite.activeweb.websockets.AppEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Util.blank;

/**
 * @author Igor Polevoy
 */
public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static Injector injector;
    private static final Map<String, Class<? extends AppEndpoint>> mappings =  new HashMap<>();

    static List<String> getControllerPackages() {
        return controllerPackages;
    }

    enum Params {
        templateManager, bootstrap, defaultLayout, targetDir, rootPackage, dbconfig, controllerConfig, rollback,
        freeMarkerConfig, route_config, maxUploadSize
    }

    private static List<String> logHeaders = new ArrayList<>();
    private static final Properties props;
    private static TemplateManager templateManager;

    private static boolean testing;

    private static final boolean activeReload = !blank(System.getProperty("active_reload")) && System.getProperty("active_reload").equals("true");
    private static AbstractFreeMarkerConfig freeMarkerConfig;
    private static boolean useDefaultLayoutForErrors = true;

    private static final List<String> controllerPackages;
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
            controllerPackages = locateControllerSubPackages();
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

    private static void initTemplateManager() {
        try{
         templateManager = (TemplateManager)Class.forName(
             get(Params.templateManager.toString())
         ).getDeclaredConstructor().newInstance();
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
            return freeMarkerConfig = (AbstractFreeMarkerConfig)Class.forName(className).getDeclaredConstructor().newInstance();
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

    /**
     * Provide names of headers to log to the log system with each request.
     * This configuration is dynamic and can be changed during runtime.
     *
     * @param headerNames list of headers to log. If the first argument is '*', all headers will be logged.
     */
    public static void logHeaders(String... headerNames) {
        logHeaders = list(headerNames);
    }

    protected static List<String> getLogHeaders(){
        return logHeaders;
    }

    /**
     * A sub-package is what you find between "app.controller" and a controller simple class name.
     * <p>
     * For instance, if the name of the controller class is <code>app.controllers.pack1.pack2.HomeController</code>,
     * then <code>pack1.pack2</code> will be returned.
     *
     * @return a list of  of sub-package names
     *
     */
    static List<String> locateControllerSubPackages()  {
        List<String> subpackages = new ArrayList<>();
        try(CloseableList<ClassInfo> infosList =  getControllerClassInfos(null)){
            for (ClassInfo classInfo : infosList) {
                String className = classInfo.getName();
                if (className.chars().filter(ch -> ch == '.').count() > 2) {
                    subpackages.add(className.substring(className.indexOf("controllers.") + 12, className.lastIndexOf('.')));
                }
            }
        }
        return subpackages;
    }


    /**
     * Returns  a list of controllers that are reachable and not abstract.
     *
     * @param classLoader - a classloader to use when looking for controllers.
     *
     * @return   list of <code>ControllerInfo</code>.
     */
    public static CloseableList<ClassInfo> getControllerClassInfos(ClassLoader classLoader){
        CloseableList<ClassInfo> controllerInfos = new CloseableList<>();
        String controllerRootPackage = Configuration.getRootPackage() + ".controllers";

        ClassGraph classGraph = new ClassGraph().acceptPackages(controllerRootPackage).enableClassInfo().enableMethodInfo().enableAnnotationInfo();

        if (classLoader != null) {
            classGraph.overrideClassLoaders(classLoader);
        }

        ScanResult scanResult = classGraph.scan();
        for (ClassInfo classInfo : scanResult.getSubclasses(AppController.class.getName())) {
            if (!classInfo.isAbstract()) {
                classInfo.getAnnotationInfo();
                controllerInfos.add(classInfo);
            }
        }

        return controllerInfos;
    }

    public static void addEndpointMapping(AbstractWebSocketConfig.EndpointMapping mapping) {
        mappings.put(mapping.getUri(), mapping.getEndpointClass());
    }

    public static Class<? extends AppEndpoint> getAppEndpointClass(String path){
         return mappings.get(path);
    }
}
