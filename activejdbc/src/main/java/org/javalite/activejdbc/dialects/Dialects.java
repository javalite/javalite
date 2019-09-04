package org.javalite.activejdbc.dialects;

import org.javalite.activejdbc.CaseInsensitiveMap;

import java.util.Map;

public class Dialects {

    private static final Map<String, Dialect> dialects = new CaseInsensitiveMap<>();

    public static Dialect getDialect(String dbType){
        Dialect dialect = dialects.get(dbType);
        if (dialect == null) {
            if(dbType.equalsIgnoreCase("Oracle")){
                dialect = new OracleDialect();
            }
            else if(dbType.startsWith("DB2")) {
                dialect = new DB2Dialect();
            }
            else if(dbType.equalsIgnoreCase("MySQL")){
                dialect = new MySQLDialect();
            }
            else if(dbType.equalsIgnoreCase("MariaDB")){
                dialect = new MySQLDialect();
            }
            else if(dbType.equalsIgnoreCase("PostgreSQL")){
                dialect = new PostgreSQLDialect();
            }
            else if(dbType.equalsIgnoreCase("h2")){
                dialect = new H2Dialect();
            }
            else if(dbType.equalsIgnoreCase("Microsoft SQL Server")){
                dialect = new MSSQLDialect();
            }
            else if(dbType.equalsIgnoreCase("SQLite")){
                dialect = new SQLiteDialect();
            }else{
                dialect = new DefaultDialect();
            }
            dialects.put(dbType, dialect);
        }
        return dialect;
    }

}
