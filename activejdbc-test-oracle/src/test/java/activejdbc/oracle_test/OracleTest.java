/*
Copyright 2009-2010 Igor Polevoy 

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


package activejdbc.oracle_test;

import activejdbc.Base;
import activejdbc.test.ActiveJDBCTest;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Base test class to reset tables and sequences.
 *
 * @author Igor Polevoy
 */
public abstract class OracleTest extends ActiveJDBCTest {
    @Override
    protected void resetDB() throws SQLException, ClassNotFoundException {

        dropTriggers();
        dropSequences();
        dropTables();
        String[] statements = getStatements("BREAK");
        Statement st = null;
        for (String statement : statements) {
            System.out.println("Executing statement: " + statement);
            st = Base.connection().createStatement();
            st.executeUpdate(statement);
        }
        st.close();
    }


    private void dropTriggers() throws SQLException {

        List<Map> triggers = Base.findAll("select trigger_name from user_triggers");
        for (Map trigger : triggers) {
            Statement s = Base.connection().createStatement();
            s.execute("drop trigger " + trigger.get("trigger_name"));
            Base.commitTransaction();
            System.out.println("Trigger dropped: " + trigger.get("trigger_name"));
            s.close();
        }
    }

    private void dropSequences() throws SQLException {

        List<Map> sequences = Base.findAll("select sequence_name from user_sequences");
        for (Map sequence : sequences) {
            Statement s = Base.connection().createStatement();
            s.execute("drop sequence " + sequence.get("sequence_name"));
            Base.commitTransaction();
            System.out.println("Sequence dropped: " + sequence.get("sequence_name"));
            s.close();
        }
    }

    private void dropTables() throws SQLException {
        List<Map> tables = Base.findAll("select table_name from user_tables");
        for (Map table : tables) {
            Statement s = Base.connection().createStatement();
            s.execute("drop table " + table.get("table_name") + " cascade constraints");
            Base.commitTransaction();
            System.out.println("Table dropped: " + table.get("table_name"));
            s.close();
        }
    }
}
