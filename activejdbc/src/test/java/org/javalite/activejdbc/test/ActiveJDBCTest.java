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

import java.io.*;
import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.javalite.common.Collections;
import org.javalite.activejdbc.*;
import org.javalite.test.jspec.JSpecSupport;

import org.junit.After;
import org.junit.Before;

import static org.javalite.activejdbc.test.JdbcProperties.*;


public abstract class ActiveJDBCTest extends JSpecSupport {

    private static boolean schemaGenerated = false;

    @Before
    public void before() throws Exception {
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
    public void after() {

        try {
            Base.connection().rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Base.close();
    }

    protected void generateSchema() throws SQLException, ClassNotFoundException {
        if (db().equals("mysql")) {
            DefaultDBReset.resetSchema(getStatements(";", "mysql_schema.sql"));
        }else if (db().equals("postgresql")) {
            DefaultDBReset.resetSchema(getStatements(";", "postgres_schema.sql"));
        } else if (db().equals("h2")) {
            DefaultDBReset.resetSchema(getStatements(";", "h2_schema.sql"));
        } else if (db().equals("oracle")) {
            OracleDBReset.resetOracle(getStatements("-- BREAK", "oracle_schema.sql"));
        } else if (db().equals("mssql")) {
        	DefaultDBReset.resetSchema(getStatements("; ", "mssql_schema.sql"));
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
     * @param year
     * @param month - 1 through 12
     * @param day   - day of month
     * @return Timestamp instance
     */
    public Timestamp getTimestamp(int year, int month, int day) {
        return new Timestamp(getTime(year, month, day));
    }


    public java.sql.Date getDate(int year, int month, int day) {
        return new java.sql.Date(getTime(year, month, day));
    }

    /**
     * there is nothing more annoying than Java date/time APIs!
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public long getTime(int year, int month, int day) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime().getTime();
    }


    protected void deleteAndPopulateTables(String... tables)  {
        for (String table : tables)
            deleteAndPopulateTable(table);
    }

    protected void deleteAndPopulateTable(String table) {
        deleteFromTable(table);
        populateTable(table);
    }

    
    protected void deleteFromTable(String table){
        executeStatements(Collections.list(getStatementProvider().getDeleteStatement(table)));
    }

    protected void populateTable(String table) {
        executeStatements(getStatementProvider().getPopulateStatements(table));
    }

    private StatementProvider getStatementProvider(){
        StatementProvider statementProvider = null;
        if (db().equals("mysql")) {
            statementProvider = new MySQLStatementProvider();
        } else if (db().equals("oracle")) {
            statementProvider = new OracleStatementProvider();
        } else if (db().equals("postgresql")) {
            statementProvider = new PostgreSQLStatementProvider();
        } else if (db().equals("h2")) {
            statementProvider = new H2StatementProvider();
        } else if (db().equals("mssql")) {
            statementProvider = new MSSQLStatementProvider();
        } else {
        	throw new RuntimeException("Unknown db:" + db());
        }
        return statementProvider;
    }

    private void close(Statement st) {
        if (st != null) { 
            try { 
                st.close(); 
            } catch (SQLException e) { 
                throw new RuntimeException(e); 
            }
        }
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
                close(st);
            }
        }
    }

    PrintStream errOrig;
    PrintStream err;
    ByteArrayOutputStream bout;

    protected void replaceSystemError() {
        errOrig = System.err;
        bout = new ByteArrayOutputStream();
        err = new PrintStream(bout);
        System.setErr(err);
    }

    protected String getSystemError() throws IOException {
        err.flush();
        bout.flush();
        return bout.toString();
    }
}
