/*
Copyright 2009-2016 Igor Polevoy

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

import java.io.*;
import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.javalite.activejdbc.cache.QueryCache;
import org.javalite.activejdbc.statement_providers.*;
import org.javalite.common.Collections;
import org.javalite.activejdbc.*;
import org.javalite.test.jspec.JSpecSupport;

import org.junit.After;
import org.junit.Before;

import static org.javalite.activejdbc.test.JdbcProperties.*;
import static org.javalite.common.Util.*;

public abstract class ActiveJDBCTest implements JSpecSupport {

    private static boolean schemaGenerated = false;

    @Before
    public final void before() throws Exception {
        Base.open(driver(), url(), user(), password());
        synchronized(this) {
            if (!schemaGenerated) {
                generateSchema();
                schemaGenerated = true;
                System.out.println("DB: " + db() + ", Driver: " + driver());
            }
        }
        Base.connection().setAutoCommit(false);
    }

    @After
    public final void after() {

        try {
            Base.connection().rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Base.close();
    }

    protected void generateSchema() throws SQLException, ClassNotFoundException {
        switch (db()) {
            case "mysql":
                DefaultDBReset.resetSchema(getStatements(";", "mysql_schema.sql"));
                break;
            case "postgresql":
                DefaultDBReset.resetSchema(getStatements(";", "postgres_schema.sql"));
                break;
            case "h2":
                DefaultDBReset.resetSchema(getStatements(";", "h2_schema.sql"));
                break;
            case "oracle":
                OracleDBReset.resetOracle(getStatements("-- BREAK", "oracle_schema.sql"));
                break;
            case "mssql":
                DefaultDBReset.resetSchema(getStatements("; ", "mssql_schema.sql"));
                break;
            case "db2":
                Db2DBReset.resetSchema(getStatements(";", "db2_schema.sql"));
                break;
            case "sqlite":
                DefaultDBReset.resetSchema(getStatements("; ", "sqlite_schema.sql"));
                break;
        }
    }

    /**
     * Returns array of strings where text was separated by semi-colons.
     *
     * @return array of strings where text was separated by semi-colons.
     */
    public String[] getStatements(String delimiter, String file) {
        try {

            System.out.println("Getting statements from file: " + file);
            InputStreamReader isr = new InputStreamReader(ActiveJDBCTest.class.getClassLoader().getResourceAsStream(file));
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder text = new StringBuilder();
            String t;
            while ((t = reader.readLine()) != null) {
                text.append(t);
                text.append('\n');
            }
            return text.toString().split(delimiter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convenience method for testing.
     *
     * @param year year
     * @param month - 1 through 12
     * @param day   - day of month
     * @return Timestamp instance
     */
    public Timestamp getTimestamp(int year, int month, int day) {
        return new Timestamp(getTime(year, month, day));
    }

    /**
     * Convenience method for testing.
     *
     * @param year year
     * @param month 1 through 12
     * @param day day of month
     * @param hour 0 through 23
     * @param minute minute
     * @param second second
     * @param millisecond millisecond
     * @return Timestamp instance
     */
    public Timestamp getTimestamp(int year, int month, int day,
            int hour, int minute, int second, int millisecond) {
        return new Timestamp(getTime(year, month, day, hour, minute, second, millisecond));
    }

    public java.sql.Date getDate(int year, int month, int day) {
        return new java.sql.Date(getTime(year, month, day));
    }

    /**
     * Convenience method for testing.
     *
     * @param year year
     * @param month 1 through 12
     * @param day day of month
     * @return time value
     */
    public long getTime(int year, int month, int day) {
        return getTime(year, month, day, 0, 0, 0, 0);
    }

    /**
     * there is nothing more annoying than Java date/time APIs!
     */
    public long getTime(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar.getTime().getTime();
    }

    protected void deleteAndPopulateTables(String... tables)  {
        for (String table : tables)
            deleteAndPopulateTable(table);
    }

    protected void deleteAndPopulateTable(String table) {
        deleteFromTable(table);
        populateTable(table);
        QueryCache.instance().purgeTableCache(table); // purge any cached query results
    }

    protected void deleteFromTable(String table){
        executeStatements(Collections.list(getStatementProvider().getDeleteStatement(table)));
    }

    protected void populateTable(String table) {
        executeStatements(getStatementProvider().getPopulateStatements(table));
    }

    private StatementProvider getStatementProvider(){
        StatementProvider statementProvider;
        switch (db()) {
            case "mysql":
                statementProvider = new MySQLStatementProvider();
                break;
            case "oracle":
                statementProvider = new OracleStatementProvider();
                break;
            case "postgresql":
                statementProvider = new PostgreSQLStatementProvider();
                break;
            case "h2":
                statementProvider = new H2StatementProvider();
                break;
            case "mssql":
                statementProvider = new MSSQLStatementProvider();
                break;
            case "db2":
                statementProvider = new DB2StatementProvider();
                break;
            case "sqlite":
                statementProvider = new SQLiteStatementProvider();
                break;
            default:
                throw new RuntimeException("Unknown db:" + db());
        }
        return statementProvider;
    }

    private void executeStatements(List<String> statements) {
        for (String statement : statements) {
            Statement st = null;
            try {
                st = Base.connection().createStatement();
                st.executeUpdate(statement);
            } catch (SQLException e) {
                throw new RuntimeException(statement, e);
            } finally {
                closeQuietly(st);
            }
        }
    }
}
