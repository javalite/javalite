/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.javalite.activejdbc.test;

import org.javalite.activejdbc.Base;
import org.javalite.common.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DB Reset for IBM DB2
 *
 * @author João Almeida
 */
public class Db2DBReset {

    static void resetSchema(String[] statements) throws SQLException {
        Connection connection = Base.connection();

        createDropTableProcedure(connection);

        for (String statement: statements) {
            if(Util.blank(statement)) continue;
            Statement st = connection.createStatement();
            st.executeUpdate(statement);
            st.close();
        }
    }

    /**
     * The dropTable procedure will be used by the db2_schema.sql in order to drop tables that might not exist
     * This functionality is not native to DB2 and, for that reason, is commonly handled using a procedure
     * @param db2Connection
     * @throws SQLException
     */
    private static void createDropTableProcedure(Connection db2Connection) throws SQLException {
        System.out.println("Creating the dropTable procedure");
        try {
            Statement stmt = db2Connection.createStatement();
            stmt.executeUpdate("create or replace procedure dropTable(IN table_name VARCHAR(50))\n"
                    + "language SQL\n"
                    + "begin\n"
                    + "  IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE TYPE = 'T' AND NAME = UPPER(table_name)) THEN\n"
                    + "     EXECUTE IMMEDIATE 'DROP TABLE ' || table_name;\n"
                    + "  END IF;\n"
                    + "end");
        } catch (SQLException e) {
            System.err.println("Error creating 'dropTable' procedure");
            throw e;
        }
    }
}
