/**
 * @author Kadvin, Date: 11-8-2 上午10:01
 */
package activejdbc;

import java.sql.Connection;

/**
 * Provide Connection
 */
public interface ConnectionProvider {
    Connection getConnection();
}
