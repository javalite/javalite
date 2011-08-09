/**
 * Developer: Kadvin Date: 11-8-9 上午3:13
 */
package org.javalite.activejdbc.providers;

import org.javalite.activejdbc.ConnectionProvider;
import org.javalite.activejdbc.InitException;
import org.javalite.activejdbc.LogFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
* Provide connection by driver, url ,properties
*/
public class AnotherProvider implements ConnectionProvider {
    private static Logger logger  = LoggerFactory.getLogger(AnotherProvider.class);
    private final String driver;
    private final String url;
    private final Properties props;

    public AnotherProvider(String driver, String url, Properties props) {
        this.driver = driver;
        this.url = url;
        this.props = props;
    }

    public Connection getConnection() {
        try {
            Class.forName(driver);
            LogFilter.log(logger, "Establish connection to: " + url);
            return DriverManager.getConnection(url, props);
        } catch (Exception e) {
            throw new InitException(e);
        }
    }
}
