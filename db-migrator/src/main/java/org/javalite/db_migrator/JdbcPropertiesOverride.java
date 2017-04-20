package org.javalite.db_migrator;


import org.javalite.common.Util;

import java.util.Properties;

/**
 * This class serves as a source of JDBC connection properties for
 * developers who cannot or do not want to use hard-coded user <code>root</code> and password <code>p@ssw0rd</code>.
 *
 * Simply add a system property when you run the build <code>-Djdbc.override.properties=/path/to/your/file.properties</code>
 * with the following properties inside the file: <code>driver</code>, <code>url</code>, <code>user</code>, <code>password</code>.
 * Values from this file will override hard-coded values during tests.
 *
 * THIS CLASS USED IN TESTS ONLY
 *
 * @author igor on 4/19/17.
 */
public class JdbcPropertiesOverride {

    //defaults:
    private static String driver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost";
    private static String user = "root";
    private static String password = "p@ssw0rd";

    static {
        try {
            String overrideFile = System.getProperty("jdbc.override.properties");
            if(overrideFile != null){
                Properties overrideProperties = Util.readProperties(overrideFile);
                driver = overrideProperties.getProperty("driver", driver);
                driver = overrideProperties.getProperty("url", url);
                driver = overrideProperties.getProperty("user", user);
                driver = overrideProperties.getProperty("password", password);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private JdbcPropertiesOverride(){
    }

    public static String driver() {
        return driver;
    }

    public static String url() {
        return url;
    }

    public static String user() {
        return user;
    }

    public static String password() {
        return password;
    }
}
