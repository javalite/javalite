/**
 * Developer: Kadvin Date: 11-8-9 上午3:13
 */
package activejdbc.providers;

import activejdbc.ConnectionProvider;
import activejdbc.InitException;
import activejdbc.LogFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

/**
* Provide connection by driver, url, user, password
*/
public class CommonProvider implements ConnectionProvider {
    private static Logger logger  = LoggerFactory.getLogger(CommonProvider.class);
    private final String driver;
    private final String url;
    private final String user;
    private final String password;

    public CommonProvider(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() {
        try {
            Class.forName(driver);
            LogFilter.log(logger, "Establish connection to: " + url);
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new InitException(e);
        }
    }
}
