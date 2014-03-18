package org.javalite.db_migrator;

import static org.javalite.common.Util.blank;
import static org.javalite.db_migrator.DatabaseType.*;


public class DatabaseUtils {
    private static final String POSTGRESQL_FRAGMENT = ":postgresql:";
    private static final String MYSQL_FRAGMENT = ":mysql:";
    private static final String HSQL_FRAGMENT = ":hsqldb:";
    private static final String H2_FRAGMENT = ":h2:";
    private static final String SQL_SERVER_JTDS_FRAGMENT = ":jtds:sqlserver:";
    private static final String SQL_SERVER_MS_2000_FRAGMENT = ":microsoft:sqlserver:";
    private static final String SQL_SERVER_MS_2005_FRAGMENT = ":sqlserver:";
    private static final String DB2_FRAGMENT = ":db2:";
    private static final String ORACLE_FRAGMENT = ":oracle:";

    /**
     * Given a jdbc url, this tries to determine the target database and returns the driver class name as a string.
     *
     * @param url jdbc url
     * @return jdbc driver class name
     */
    public static String driverClass(String url) {
        assert !blank(url);

        if (url.contains(POSTGRESQL_FRAGMENT)) {
            return "org.postgresql.Driver";
        }
        if (url.contains(MYSQL_FRAGMENT)) {
            return "com.mysql.jdbc.Driver";
        }
        if (url.contains(HSQL_FRAGMENT)) {
            return "org.hsqldb.jdbcDriver";
        }
        if (url.contains(H2_FRAGMENT)) {
            return "org.h2.Driver";
        }
        if (url.contains(SQL_SERVER_JTDS_FRAGMENT)) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }
        if (url.contains(SQL_SERVER_MS_2000_FRAGMENT)) {
            return "com.microsoft.jdbc.sqlserver.SQLServerDriver";
        } // 2000
        if (url.contains(SQL_SERVER_MS_2005_FRAGMENT)) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } // 2005
        if (url.contains(DB2_FRAGMENT)) {
            return "com.ibm.db2.jcc.DB2Driver";
        }
        if (url.contains(ORACLE_FRAGMENT)) {
            return "oracle.jdbc.driver.OracleDriver";
        }

        return null;
    }

    public static DatabaseType databaseType(String url) {
        assert !blank(url);

        if (url.contains(POSTGRESQL_FRAGMENT)) {
            return POSTGRESQL;
        }
        if (url.contains(MYSQL_FRAGMENT)) {
            return MYSQL;
        }
        if (url.contains(HSQL_FRAGMENT)) {
            return HSQL;
        }
        if (url.contains(H2_FRAGMENT)) {
            return H2;
        }
        if (url.contains(SQL_SERVER_JTDS_FRAGMENT)) {
            return SQL_SERVER;
        }
        if (url.contains(SQL_SERVER_MS_2000_FRAGMENT)) {
            return SQL_SERVER;
        }
        if (url.contains(SQL_SERVER_MS_2005_FRAGMENT)) {
            return SQL_SERVER;
        }
        if (url.contains(DB2_FRAGMENT)) {
            return DB2;
        }
        if (url.contains(ORACLE_FRAGMENT)) {
            return ORACLE;
        }

        return UNKNOWN;
    }

    /**
     * Given a JDBC connection URL, extract only the database name.
     *
     * @param url a JDBC connection URL
     * @return the database name
     */
    //TODO: man, this is ugly - igor
    public static String extractDatabaseName(String url) {
        int leftIndex = url.lastIndexOf("/");
        if (leftIndex == -1) {
            leftIndex = url.lastIndexOf(":");
        }
        leftIndex++;

        int rightIndex = url.length();
        if (url.contains("?")) {
            rightIndex = url.indexOf("?");
        } else if (url.contains(";")) {
            rightIndex = url.indexOf(";");
        }
        return url.substring(leftIndex, rightIndex);
    }

    /**
     * Given a JDBC connection URL, generate a new connection URL to connect directly to the database server itself (ie: no database specified).
     *
     * @param url a JDBC connection URL
     * @return a new JDBC connection URL to connect directly to the database server
     */
    public static String extractServerUrl(String url)
    {
        int rightIndex = url.length();
        if (url.lastIndexOf("/") != -1) { rightIndex = url.lastIndexOf("/"); }
        else if (url.lastIndexOf(":") != -1) { rightIndex = url.lastIndexOf(":"); }

        String baseUrl = url.substring(0, rightIndex);

        // TODO This next line is pretty ugly, but it works for nearly every postgresql server.
        // If we have to add another exception to this for another database server, then I highly recommend refactoring this to a more elegant solution.
        if (POSTGRESQL.equals(databaseType(url))) { baseUrl += "/postgres"; }

        String options = "";
        int optionsIndex = url.indexOf("?");
        if (optionsIndex == -1) { optionsIndex = url.indexOf(";"); }
        if (optionsIndex != -1) { options = url.substring(optionsIndex); }

        return baseUrl + options;
    }

}
