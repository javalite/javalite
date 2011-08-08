/**
 * @author Kadvin, Date: 11-8-2 上午10:01
 */
package activejdbc;

import java.sql.Connection;

/**
 * Provide Connection, it likes a connection specific maintainer which can provide connection later.
 */
public interface ConnectionProvider {
    /**
     * Provide connection to caller
     *
     * @return the usable connection
     */
    Connection getConnection();
}
