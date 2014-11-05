package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;

import static org.javalite.db_migrator.DatabaseType.HSQL;
import static org.javalite.db_migrator.DatabaseType.SQL_SERVER;
import static java.lang.String.format;

/**
 * A trivial VersionStrategy which tracks only the minimal information required to note the current state of the database: the current version.
 */
public class VersionStrategy {
    public static final String VERSION_TABLE = "schema_version";
    public static final String VERSION_COLUMN = "version";
    public static final String APPLIED_DATE_COLUMN = "applied_on";
    public static final String DURATION_COLUMN = "duration";

    private static final Logger logger = LoggerFactory.getLogger(VersionStrategy.class);


    private static final Map CREATE_VERSION_TABLE_MAP;

    static class DefaultMap extends HashMap<DatabaseType, String>{
        private String defaultValue = "create table %s (%s varchar(32) not null unique, %s timestamp not null, %s int not null)";
        @Override
        public String get(Object key) {
            return containsKey(key)? super.get(key): defaultValue;
        }
    }

    static {
        CREATE_VERSION_TABLE_MAP = new DefaultMap();
        CREATE_VERSION_TABLE_MAP.put(HSQL, "create table %s (%s varchar not null, %s datetime not null, %s int not null, constraint %2$s_unique unique (%2$s))");
        CREATE_VERSION_TABLE_MAP.put(SQL_SERVER, "create table %s (%s varchar(32) not null unique, %s datetime not null, %s int not null)");
    }

    public boolean isEnabled() {
        try {
            Base.count(VERSION_TABLE);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void createSchemaVersionTable(DatabaseType dbType) {
        String ddl = format((String) CREATE_VERSION_TABLE_MAP.get(dbType), VERSION_TABLE, VERSION_COLUMN, APPLIED_DATE_COLUMN, DURATION_COLUMN);
        Base.exec(ddl);
    }

    public List<String> getAppliedMigrations() {
        // Make sure migrations is enabled.
        if (!isEnabled()) {
            return new ArrayList<String>();
        } else {
            return (List<String>) Base.firstColumn("select " + VERSION_COLUMN + " from " + VERSION_TABLE);
        }
    }

    //todo: one line method.
    public void recordMigration(String version, Date startTime, long duration) {
        Base.exec("insert into " + VERSION_TABLE + " values (?, ?, ?)", version, new Timestamp(startTime.getTime()), duration);
    }
}
