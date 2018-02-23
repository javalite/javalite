package org.javalite.db_migrator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;

import static java.lang.String.format;
import static org.javalite.db_migrator.DatabaseType.CLICKHOUSE;
import static org.javalite.db_migrator.DatabaseType.HSQL;
import static org.javalite.db_migrator.DatabaseType.SQL_SERVER;
import static org.javalite.db_migrator.DbUtils.*;


/**
 * A trivial VersionStrategy which tracks only the minimal information required to note the current state of the database: the current version.
 */
public class VersionStrategy {
    public static final String VERSION_TABLE = "schema_version";
    public static final String VERSION_COLUMN = "version";
    public static final String APPLIED_DATE_COLUMN = "applied_on";
    public static final String DURATION_COLUMN = "duration";
    private static final String DEFAULT_VALUE = "create table %s (%s varchar(32) not null unique, %s timestamp not null, %s int not null)";

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionStrategy.class);

    private static final Map<DatabaseType, String> CREATE_VERSION_TABLE_MAP;

    static {
        CREATE_VERSION_TABLE_MAP = new HashMap<>();
        CREATE_VERSION_TABLE_MAP.put(HSQL, "create table %s (%s varchar not null, %s datetime not null, %s int not null, constraint %2$s_unique unique (%2$s))");
        CREATE_VERSION_TABLE_MAP.put(SQL_SERVER, "create table %s (%s varchar(32) not null unique, %s datetime not null, %s int not null)");
        CREATE_VERSION_TABLE_MAP.put(CLICKHOUSE, "create table %s (%s String, %s DateTime, %s Int32) engine = Log");
    }


    public void createSchemaVersionTable(DatabaseType dbType) {
        LOGGER.info("Creating schema version table for {} DB", dbType);
        String ddl = format(CREATE_VERSION_TABLE_MAP.getOrDefault(dbType, DEFAULT_VALUE), VERSION_TABLE, VERSION_COLUMN, APPLIED_DATE_COLUMN, DURATION_COLUMN);
        exec(ddl);
        if (dbType == CLICKHOUSE) {
            LOGGER.info("Additing empty row for ClickHouse count to work correctly");
            exec("insert into schema_version values ('20161202153800', '2016-12-02 15:38:00',1)");//ClickHouse returns empty result set in case of count empty table
        }
    }

    public boolean versionTableExists() {
        try {
            countRows(VERSION_TABLE);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public List<String> getAppliedMigrations()  {
        Statement st = null;
        ResultSet rs = null;
        try{
            st = connection().createStatement();
            rs = st.executeQuery("select " + VERSION_COLUMN + " from " + VERSION_TABLE);
            List<String> migrations = new ArrayList<String>();
            while (rs.next()){
                migrations.add(rs.getString(1));
            }
            return migrations;
        }catch(SQLException e){
            throw new MigrationException(e);
        }finally {
            closeQuietly(rs);
            closeQuietly(st);
        }
    }

    public void recordMigration(String version, Date startTime, long duration) {
        PreparedStatement st = null;
        try{
            st = connection().prepareStatement("insert into " + VERSION_TABLE + " values (?, ?, ?)");
            st.setString(1, version);
            st.setTimestamp(2, new Timestamp(startTime.getTime()));
            st.setLong(3, duration);
            st.execute();
        }catch(Exception e){
            throw  new MigrationException(e);
        }finally {
            closeQuietly(st);
        }
    }
}
