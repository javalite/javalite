/**
 * Developer: Kadvin Date: 11-8-9 上午3:14
 */
package activejdbc.providers;

import activejdbc.ConnectionProvider;
import activejdbc.InitException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

/**
* Provide connection by jdni name
*/
public class ByJndiProvider implements ConnectionProvider {

    private final String jndiName;

    public ByJndiProvider(String jndiName) {
        this.jndiName = jndiName;
    }

    public Connection getConnection() {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(jndiName);
            return ds.getConnection();
        } catch (Exception e) {
            throw new InitException(e);
        }
    }
}
