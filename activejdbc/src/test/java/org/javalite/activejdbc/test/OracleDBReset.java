package org.javalite.activejdbc.test;

import org.javalite.activejdbc.Base;
import org.javalite.common.Util;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Util.*;

/**
 * @author Igor Polevoy
 */
public class OracleDBReset {

    static void resetOracle(String[] statements) throws SQLException {
            dropTriggers();
            dropSequences();
            dropTables();

            for (String statement : statements) {
                if (!Util.blank(statement)) {
                    Statement st = null;
                    try {
                        st = Base.connection().createStatement();
                        st.executeUpdate(statement);
                    } catch(SQLException e) {
                        System.out.println("Problem statement: " + statement);
                        throw e;
                    } finally {
                        closeQuietly(st);
                    }
                }
            }
    }


    private static void dropTriggers() throws SQLException {

        List<Map<String, Object>> triggers = Base.findAll("select trigger_name from user_triggers");
        for (Map trigger : triggers) {
            if(trigger.get("trigger_name").toString().contains("$")){
                continue;   
            }
            Statement s = Base.connection().createStatement();
            String sql = "drop trigger " + trigger.get("trigger_name");
            System.out.println("Dropping trigger: " + trigger.get("trigger_name"));
            s.execute(sql);
            s.close();
        }
    }

    private static void dropSequences() throws SQLException {

        List<Map<String, Object>> sequences = Base.findAll("select sequence_name from user_sequences");
        for (Map sequence : sequences) {
            String sequenceName = sequence.get("sequence_name").toString();
            // Skip system sequences (LogMiner, REDO, etc.)
            if(sequenceName.contains("$") || sequenceName.startsWith("LOGMNR") || 
               sequenceName.startsWith("REDO") || sequenceName.startsWith("SYS_")) {
                continue;
            }
            Statement s = Base.connection().createStatement();
            s.execute("drop sequence " + sequenceName);
            System.out.println("Dropping sequence: " + sequenceName);
            s.close();
        }
    }

    private static void dropTables() throws SQLException {
        List<Map<String, Object>> tables = Base.findAll("select table_name from user_tables");
        for (Map<String, Object> table : tables) {
            String tableName = table.get("table_name").toString();
            // Skip system tables (LogMiner, REDO, Scheduler, Replication, etc.)
            if(tableName.contains("$") || tableName.startsWith("LOGMNR") || 
               tableName.startsWith("REDO") || tableName.startsWith("SYS_") ||
               tableName.startsWith("BIN$") || tableName.startsWith("SCHEDULER_") ||
               tableName.startsWith("APEX_") || tableName.startsWith("WRI$_") ||
               tableName.startsWith("QUEST_") || tableName.startsWith("DBMS_") ||
               tableName.startsWith("REPL_") || tableName.startsWith("REPCAT_") ||
               tableName.startsWith("DEF$_") || tableName.startsWith("MLOG$_") ||
               tableName.startsWith("SQLPLUS_") || tableName.startsWith("PRODUCT_") ||
               tableName.startsWith("HELP") || tableName.equals("PLAN_TABLE")) {
                continue;
            }
            Statement s = Base.connection().createStatement();
            s.execute("drop table " + tableName + " cascade constraints");
            System.out.println("Dropping table: " + tableName);
            s.close();
        }
    }
}
