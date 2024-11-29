package org.javalite.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.javalite.activeweb.RequestDispatcher;
import org.javalite.app_config.AppConfig;

import java.util.List;

import static org.javalite.app_config.AppConfig.p;
import static org.javalite.app_config.AppConfig.pInteger;

/**
 * Embedded Tomcat preconfigured for JavaLite. For the most part, this should work as expected.
 * If you need further customization, feel free to create your version by subclassing this class
 * and overriding any methods.
 */
public class EmbeddedTomcat {

    private final Tomcat tomcat;
    private Context context;

    public EmbeddedTomcat() {
        tomcat = new Tomcat();
        configure();
    }

    /**
     * Configures instance of the container programmatically.
     */
    protected void configure(){
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));
        tomcat.enableNaming();
        tomcat.setPort(pInteger("embedded.tomcat.port"));
        Connector connector = tomcat.getConnector();
        configureConnector(connector);
        context = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
        configureRequestDispatcher(context);
        Tomcat.addServlet(context, "default", new DefaultServlet());
        context.addServletMappingDecoded("/", "default");

        // Configure JNDI DataSource in the context
        context.getNamingResources().addResource(configureDBConnectionPool());
    }

    /**
     * Generally used in JavaLite to configure an instance of a tomcat Connector from AppConfig properties.
     * Each property name has two parts:
     * <code>embedded.tomcat.connector</code> and the actual property name. This method will look for all properties
     * whose name starts with "embedded.tomcat.connector", will strip <code>embedded.tomcat.connector</code> prefix
     * and will apply the property to the Tomcat Connector before the start.
     *
     * <br>
     * The following are some properties for configuring the embedded Tomcat Connector.
     * <pre>
     * embedded.tomcat.connector.maxConnections=100
     * embedded.tomcat.connector.maxThreads=50
     * </pre>
     * You can add more properties here as long as they correspond to the
     * <a href="https://tomcat.apache.org/tomcat-11.0-doc/config/http.html">Tomcat Connector Configuration</a>.
     *
     * @param connector instance of the connector. This method is obvious has a side effect, fyi.
     */
    protected void configureConnector(Connector connector) {
        List<String> tomcatKeys = AppConfig.getKeys("embedded.tomcat.connector");
        for (String propertyName : tomcatKeys) {
            String tomcatName = propertyName.substring("embedded.tomcat.connector.".length());
            String val = AppConfig.p(propertyName);
            connector.setProperty(tomcatName, val);
        }
    }

    /**
     * Starts the container.
     *
     * @throws LifecycleException thrown if fails to start
     */
    public void start() throws LifecycleException {
        tomcat.start();
        tomcat.getServer().await();
    }

    /**
     * Stops the container.
     *
     * @throws LifecycleException thrown if fails to stop
     */
    public void stop() throws LifecycleException {
        tomcat.stop();
        tomcat.getServer().stop();
    }

    /**
     * Sets up JavaLite ActiveWeb filter on the container.
     *
     * @param context   object to attach the filter to.
     */
    protected void configureRequestDispatcher(Context context) {
        FilterDef filterDef = new FilterDef();
        filterDef.addInitParameter("exclusions", p("embedded.tomcat.filter.exclusions"));
        filterDef.addInitParameter("root_controller", p("embedded.tomcat.home.controller"));

        filterDef.setFilterName(RequestDispatcher.class.getSimpleName());
        filterDef.setFilterClass(RequestDispatcher.class.getName());
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(RequestDispatcher.class.getSimpleName());
        filterMap.addURLPattern("/*");
        context.addFilterMap(filterMap);
    }

    /**
     * Generally used in JavaLite to configure a database connection pool from AppConfig properties.
     * The following are some properties for configuring the embedded Tomcat DBCP pool.
     * Each property name has two parts:
     * <code>embedded.tomcat.pool</code> and the actual property name. This method will look for all properties
     * whose name starts with "embedded.tomcat.pool", will strip this prefix and will apply the property to
     * the Tomcat instance at the start.  You can add more properties here as long as they correspond to the
     * <a href="https://commons.apache.org/proper/commons-dbcp/configuration.html">DBCP Pool Configuration</a>.
     *
     * <br>
     * Example properties:
     * <pre>
     * embedded.tomcat.pool.driverClassName=org.mariadb.jdbc.Driver
     * embedded.tomcat.pool.maxIdle=5
     * embedded.tomcat.pool.maxTotal=50
     * embedded.tomcat.pool.minIdle=2
     * embedded.tomcat.pool.initialSize=10
     * embedded.tomcat.pool.password=p@ssw0rd
     * embedded.tomcat.pool.username=root
     * embedded.tomcat.pool.url=jdbc:mariadb://localhost:3309/javalite_tomcat
     * embedded.tomcat.pool.name=jdbc/myDatabasePool
     * </pre>
     *
     * @return configured instance of <code>ContextResource</code>.
     */
    protected ContextResource configureDBConnectionPool() {

        ContextResource resource = new ContextResource();
        resource.setName(p("embedded.tomcat.pool.name"));
        resource.setAuth("Container");
        resource.setType("javax.sql.DataSource");
        resource.setProperty("factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory"); // see inside for additional properties

        List<String> tomcatKeys = AppConfig.getKeys("embedded.tomcat.pool");
        tomcatKeys.remove("embedded.tomcat.port");
        tomcatKeys.remove("embedded.tomcat.pool.name");

        for (String propertyName : tomcatKeys) {
            String tomcatName = propertyName.substring("embedded.tomcat.pool.".length());
            String val = AppConfig.p(propertyName);
            resource.setProperty(tomcatName, val);
        }
        return resource;
    }

    public Tomcat getTomcat() {
 		return tomcat;   	
    }

    public Context getContext() {
    	return context;
    }
    
}
