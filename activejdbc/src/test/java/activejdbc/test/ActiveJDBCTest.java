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


package activejdbc.test;

import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import activejdbc.Base;
import activejdbc.MySQLStatementProvider;
import activejdbc.OracleStatementProvider;
import activejdbc.PostgreSQLStatementProvider;
import javalite.test.jspec.JSpecSupport;

import org.junit.After;
import org.junit.Before;

import static activejdbc.test.JdbcProperties.*;


public abstract class ActiveJDBCTest extends JSpecSupport {

    static boolean schemaGenerated = false;

    @Before
    public void before() throws Exception {
        Base.open(driver(), url(), user(), password());

        if(!schemaGenerated){
            generateSchema();
            schemaGenerated = true;
            System.out.println("DB: " + db());
        }
    }

    @After
    public void after() {
        Base.close();
    }

    protected void generateSchema() throws SQLException, ClassNotFoundException {
        if (db().equals("mysql")) {
            DefaultDBReset.resetSchema(getStatements(";", "mysql_schema.sql"));
        }else if (db().equals("postgresql")) {
            DefaultDBReset.resetSchema(getStatements(";", "postgres_schema.sql"));
        } else if (db().equals("oracle")) {
            OracleDBReset.resetOracle(getStatements("-- BREAK", "oracle_schema.sql"));
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
            StringBuffer text = new StringBuffer();
            String t;
            while ((t = reader.readLine()) != null) {
                text.append(t + '\n');
            }
            return text.toString().split(delimiter);
        }
        catch (Exception e) {
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


    protected void resetTables(String... tables)  {
        for (String table : tables)
            resetTable(table);
    }

    protected void resetTable(String table) {
        List<String> statements = null;
        if (db().equals("mysql")) {
            statements = new MySQLStatementProvider().getStatements(table);
        } else if (db().equals("oracle")) {
            statements = new OracleStatementProvider().getStatements(table);
        } else if (db().equals("postgresql")) {
            statements = new PostgreSQLStatementProvider().getStatements(table);
        }
        executeStatements(statements);
    }

    private void executeStatements(List<String> statements) {
        try {            
            for (String statement : statements) {
                Statement st;
                st = Base.connection().createStatement();
                try{
                    st.executeUpdate(statement);
                }
                catch(Exception e){
                    System.out.println("Statement: " + statement);
                    throw e;
                }

                st.close();
            }            
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
