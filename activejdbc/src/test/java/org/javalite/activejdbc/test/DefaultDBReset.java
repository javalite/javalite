package org.javalite.activejdbc.test;

import org.javalite.activejdbc.Base;
import org.javalite.common.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.javalite.activejdbc.test.JdbcProperties.*;
import static org.javalite.activejdbc.test.JdbcProperties.driver;
import static org.javalite.activejdbc.test.JdbcProperties.password;

/**
 * @author Igor Polevoy
 */
public class DefaultDBReset {


    private static boolean schemaGenerated = false;

    protected static void before() throws SQLException {
        Base.open(driver(), url(), user(), password());
        System.out.println("Connecting to: " + url() + " with driver: " + driver() + ", db: " + db());
        System.out.println("Generating schema");
        tryGenSchema();
        System.out.println("Generating schema completed");
        Base.connection().setAutoCommit(false);
    }

    protected static void tryGenSchema() throws SQLException {
        if (!schemaGenerated) {
            generateSchema();
            schemaGenerated = true;
        }
    }




    public static void after() {

        try {
            Base.connection().rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Base.close();
    }


    private static void resetSchema(String[] statements) throws SQLException {
        Connection connection = Base.connection();
        for (String statement : statements) {
            if (!Util.blank(statement)) {
                Statement st = connection.createStatement();
                st.executeUpdate(statement);
                st.close();
            }
        }
    }


    private static void generateSchema() throws SQLException {
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
    private static String[] getStatements(String delimiter, String file) {
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

}
