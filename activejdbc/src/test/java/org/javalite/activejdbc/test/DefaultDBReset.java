package org.javalite.activejdbc.test;

import org.javalite.activejdbc.Base;
import org.javalite.common.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Igor Polevoy
 */
public class DefaultDBReset {

    static void resetSchema(String[] statements) throws SQLException {
        Connection connection = Base.connection();
        for (String statement: statements) {
            if (!Util.blank(statement)) {
                Statement st = connection.createStatement();
                st.executeUpdate(statement);
                st.close();
            }
        }        
    }
    
}
