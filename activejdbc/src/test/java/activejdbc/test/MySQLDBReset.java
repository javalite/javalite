package activejdbc.test;

import activejdbc.Base;
import javalite.common.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Igor Polevoy
 */
public class MySQLDBReset {

    static void resetMySQL(String[] statements) throws SQLException {
        Connection connection = Base.connection();
        for (String statement: statements) {
            if(Util.blank(statement)) continue;
            Statement st = connection.createStatement();
            st.executeUpdate(statement);
            st.close();
        }        
    }
    
}
