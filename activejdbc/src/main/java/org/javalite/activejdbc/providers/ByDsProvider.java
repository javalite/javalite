/**
 * Developer: Kadvin Date: 11-8-9 上午3:14
 */
package org.javalite.activejdbc.providers;

import org.javalite.activejdbc.ConnectionProvider;
import org.javalite.activejdbc.InitException;

import javax.sql.DataSource;
import java.sql.Connection;

/**
* Provide connection by data source
*/
public class ByDsProvider implements ConnectionProvider {
    private final DataSource ds;

    public ByDsProvider(DataSource ds) {
        this.ds = ds;
    }

    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (Exception e) {
            throw new InitException(e);
        }
    }
}
