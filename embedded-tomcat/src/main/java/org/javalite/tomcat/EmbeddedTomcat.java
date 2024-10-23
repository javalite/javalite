package org.javalite.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
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
        tomcat.getConnector();
        Context context = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
        setupJavaServletFilter(context);
        Tomcat.addServlet(context, "default", new DefaultServlet());
        context.addServletMappingDecoded("/", "default");

        // Configure JNDI DataSource in the context
        context.getNamingResources().addResource(prepareContextResources());
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
    protected void setupJavaServletFilter(Context context) {
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
     * Generally used in JavaLite to configure a database connection pool.
     *
     * @return configured instance of <code>ContextResource</code>.
     */
    protected ContextResource prepareContextResources() {

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
}