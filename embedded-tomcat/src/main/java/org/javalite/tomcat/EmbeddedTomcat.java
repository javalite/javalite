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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.javalite.app_config.AppConfig.p;
import static org.javalite.app_config.AppConfig.pInteger;

/**
 * Embedded Tomcat preconfigured for JavaLite. For the most part, this should work as expected.
 * If you need further customization, feel free to create your version by subclassing this class
 * and overriding any methods.
 *
 * <h2>Configuration</h2>
 *
 * All configuration is driven by {@link AppConfig} properties. Properties can come from any combination of:
 * <ul>
 *     <li>Classpath property files (<code>/app_config/development.properties</code>, etc.)</li>
 *     <li>An external property file via <code>-Dapp_config.properties=/path/to/file.properties</code></li>
 *     <li>A custom {@link org.javalite.app_config.AppConfigProvider} (e.g. AWS Secrets Manager)</li>
 *     <li>Environment variables (highest precedence, override all other sources)</li>
 * </ul>
 *
 * <h2>Environment Variable Naming</h2>
 *
 * Because environment variable names cannot contain dots, AppConfig translates between the two conventions:
 * dots and dashes in property names become underscores, and letters are uppercased.
 * For example, <code>embedded.tomcat.port</code> maps to the environment variable
 * <code>EMBEDDED_TOMCAT_PORT</code>. Both uppercase and lowercase variants are accepted.
 *
 * <h2>Property Reference</h2>
 *
 * <h3>Core</h3>
 * <pre>
 * embedded.tomcat.port=8080
 * embedded.tomcat.filter.exclusions=/images,/css,/js
 * embedded.tomcat.home.controller=home
 * </pre>
 *
 * <h3>Connector (see <a href="https://tomcat.apache.org/tomcat-11.0-doc/config/http.html">Tomcat Connector docs</a>)</h3>
 * <pre>
 * embedded.tomcat.connector.maxConnections=100
 * embedded.tomcat.connector.maxThreads=50
 * </pre>
 *
 * <h3>Connection pool (see <a href="https://commons.apache.org/proper/commons-dbcp/configuration.html">DBCP docs</a>)</h3>
 * <pre>
 * embedded.tomcat.pool.name=jdbc/myPool
 * embedded.tomcat.pool.driverClassName=org.mariadb.jdbc.Driver
 * embedded.tomcat.pool.url=jdbc:mariadb://localhost:3306/mydb
 * embedded.tomcat.pool.username=root
 * embedded.tomcat.pool.password=secret
 * embedded.tomcat.pool.maxTotal=50
 * embedded.tomcat.pool.maxIdle=5
 * embedded.tomcat.pool.minIdle=2
 * embedded.tomcat.pool.initialSize=10
 * </pre>
 *
 * <h2>Configuration examples</h2>
 *
 * <h3>Property file only</h3>
 * Define all properties in <code>production.properties</code> and deploy normally.
 *
 * <h3>Environment variables only</h3>
 * <pre>
 * export EMBEDDED_TOMCAT_PORT=8080
 * export EMBEDDED_TOMCAT_POOL_URL=jdbc:mariadb://db-host:3306/mydb
 * export EMBEDDED_TOMCAT_POOL_PASSWORD=secret
 * </pre>
 * No property file is required. {@link AppConfig#p(String)} and {@link AppConfig#getKeys(String)} both
 * resolve env-var-only properties via name translation, so connector and pool configuration
 * discovered through {@code getKeys("embedded.tomcat.connector")} and
 * {@code getKeys("embedded.tomcat.pool")} will include env-var-only entries.
 *
 * <p><strong>Limitation:</strong> Connector and pool property names passed to Tomcat are derived
 * by stripping the prefix from the property key. When a property exists only as an environment
 * variable (never declared in a property file), the translation to lowercase loses camelCase
 * information — for example, {@code EMBEDDED_TOMCAT_CONNECTOR_MAXTHREADS} translates to
 * {@code maxthreads}, but Tomcat expects {@code maxThreads}. In practice this rarely matters:
 * the properties most commonly supplied via environment variables are secrets such as
 * {@code password}, {@code url}, and {@code username}, which are all lowercase. Tuning parameters
 * with camelCase names (e.g. {@code maxThreads}, {@code connectionTimeout}) are not sensitive
 * and are normally declared in a property file. For edge cases where full env-var-only
 * configuration of camelCase properties is required, implement a custom
 * {@link org.javalite.app_config.AppConfigProvider} to load those properties from any source
 * and return them with the correct property names.</p>
 *
 * <h3>Mixed: files for defaults, env vars for secrets</h3>
 * Commit non-sensitive defaults to a property file:
 * <pre>
 * embedded.tomcat.port=8080
 * embedded.tomcat.pool.driverClassName=org.mariadb.jdbc.Driver
 * embedded.tomcat.pool.url=jdbc:mariadb://localhost:3306/mydb
 * embedded.tomcat.pool.maxTotal=50
 * </pre>
 * Then supply secrets at runtime via environment variables, which override the file values:
 * <pre>
 * export EMBEDDED_TOMCAT_POOL_USERNAME=root
 * export EMBEDDED_TOMCAT_POOL_PASSWORD=secret
 * </pre>
 */
public class EmbeddedTomcat {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedTomcat.class);

    private final Tomcat tomcat;
    private Context context;

    public EmbeddedTomcat() {
        tomcat = new Tomcat();
        configure();
    }

    /**
     * Validates that all required configuration properties are present.
     * Collects every missing property before throwing, so the developer sees everything
     * that needs to be set in one shot rather than fixing one property at a time.
     *
     * <p>This method is {@code public static} so it can also be called directly from tests
     * or application startup code without instantiating a full container.</p>
     *
     * @throws IllegalStateException listing all missing required properties, including the
     *         equivalent environment variable name for each one.
     */
    public static void validateRequiredProperties() {
        String[] required = {
            "embedded.tomcat.port",
            "embedded.tomcat.pool.name",
            "embedded.tomcat.pool.driverClassName",
            "embedded.tomcat.pool.url",
            "embedded.tomcat.pool.username",
            "embedded.tomcat.pool.password"
        };

        List<String> missing = new ArrayList<>();
        for (String key : required) {
            if (p(key) == null) {
                missing.add(key);
            }
        }

        if (!missing.isEmpty()) {
            String missingList = missing.stream()
                .map(k -> "  - " + k + "  (env var: " + AppConfig.propertyNameToEnvVarName(k) + ")")
                .collect(Collectors.joining("\n"));
            throw new IllegalStateException(
                "EmbeddedTomcat cannot start. The following required properties are missing:\n" +
                missingList + "\n" +
                "Set them in your properties file (e.g. production.properties) or as the " +
                "corresponding environment variables shown above."
            );
        }
    }

    /**
     * Configures instance of the container programmatically.
     */
    protected void configure(){
        validateRequiredProperties();
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
        String exclusions = p("embedded.tomcat.filter.exclusions");
        if (exclusions == null) {
            logger.warn("Property 'embedded.tomcat.filter.exclusions' is not set. No URL exclusions will be applied to the RequestDispatcher filter.");
        }
        filterDef.addInitParameter("exclusions", exclusions);

        String homeController = p("embedded.tomcat.home.controller");
        if (homeController == null) {
            logger.warn("Property 'embedded.tomcat.home.controller' is not set. Requests to '/' will not be routed to any controller.");
        }
        filterDef.addInitParameter("root_controller", homeController);

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
