package org.javalite.activejdbc.test;

import org.javalite.activejdbc.Base;
import org.javalite.common.Util;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class OracleDBReset {

    private static boolean done = false;

    public static void resetOracle(String[] statements) throws SQLException {
            dropTriggers();
            dropSequences();
            dropTables();

            for (String statement : statements) {
                if(Util.blank(statement)) continue;
                Statement st = null;
                st = Base.connection().createStatement();
                try{
                    st.executeUpdate(statement);

                    if(statement.contains("ingredients_trigger")){
                        System.out.println("STATEMENT:");
                        System.out.println(statement);
                    }

                }catch(SQLException e){
                    System.out.println("Problem statement: " + statement);
                    throw e;
                }

                st.close();
            }
    }


    private static void dropTriggers() throws SQLException {

        List<Map> triggers = Base.findAll("select trigger_name from user_triggers");
        for (Map trigger : triggers) {
            if(trigger.get("trigger_name").toString().contains("$")){
                continue;   
            }
            Statement s = Base.connection().createStatement();
            String sql = "drop trigger " + trigger.get("trigger_name");
            s.execute(sql);
            s.close();
        }
    }

    private static void dropSequences() throws SQLException {

        List<Map> sequences = Base.findAll("select sequence_name from user_sequences");
        for (Map sequence : sequences) {
            Statement s = Base.connection().createStatement();
            s.execute("drop sequence " + sequence.get("sequence_name"));
            s.close();
        }
    }

    private static void dropTables() throws SQLException {
        List<Map> tables = Base.findAll("select table_name from user_tables");
        for (Map table : tables) {
            Statement s = Base.connection().createStatement();
            s.execute("drop table " + table.get("table_name") + " cascade constraints");
            s.close();
        }
    }
}
