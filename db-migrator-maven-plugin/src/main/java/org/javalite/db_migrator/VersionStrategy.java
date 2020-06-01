package org.javalite.db_migrator;


import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
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
    }

    public boolean versionTableExists() {
        try {
            countRows(VERSION_TABLE);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public List<String> getAppliedMigrations() {
        return Base.firstColumn("select " + VERSION_COLUMN + " from " + VERSION_TABLE);
    }

    public void recordMigration(String version, Date startTime, long duration) {

        try{
            Base.exec("insert into " + VERSION_TABLE + " values (?, ?, ?)", version, new Timestamp(startTime.getTime()), duration);
        }catch(Exception e){
            throw  new MigrationException(e);
        }
    }
}
