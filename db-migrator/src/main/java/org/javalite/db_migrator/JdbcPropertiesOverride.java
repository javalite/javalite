package org.javalite.db_migrator;


import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.javalite.db_migrator.MigrationManager.MIGRATION_LOGGER;

/**
 * This class serves as a source of JDBC connection properties for different environments during tests.
 *
 * THIS CLASS USED IN TESTS ONLY.
 *
 * @author igor on 4/19/17.
 */
public class JdbcPropertiesOverride {


    private static final String driver;
    private static String url;
    private static final String fullUrl;
    private static final String user;
    private static final String password;

    static {
        try {
            String file = System.getProperty("jdbc.properties.file");
            if(file != null){
                MIGRATION_LOGGER.info("Located  database connection config file: " + file);
                Properties props = Util.readProperties(System.getProperty("jdbc.properties.file"));
                fullUrl = props.getProperty("development.url");// this includes the database name. We need to remove it.
                JdbcPropertiesOverride.url = fullUrl.substring(0, fullUrl.lastIndexOf("/"));
                driver = props.getProperty("development.driver");
                user = props.getProperty("development.username");
                password = props.getProperty("development.password");
            }else {
                throw new RuntimeException("Failed to find a config file");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String driver() {
        return driver;
    }

    /**
     * @return the URL to the database, but does not include a database name.
     *      If  this  URL was configured in the file: <code>jdbc:mysql://localhost/test_project</code>, then the returned value will be:
     *      </code>jdbc:mysql://localhost</code>
     */
    public static String url() {
        return url;
    }

    public static String fullUrl() {
        return fullUrl;
    }
    public static String user() {
        return user;
    }

    public static String password() {
        return password;
    }
}
