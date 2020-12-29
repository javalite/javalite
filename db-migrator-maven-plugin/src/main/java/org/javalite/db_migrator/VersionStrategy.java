package org.javalite.db_migrator;


import org.javalite.activejdbc.Base;
import org.javalite.common.Convert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.javalite.db_migrator.DatabaseType.*;

import static org.javalite.db_migrator.DbUtils.countMigrations;
import static org.javalite.db_migrator.DbUtils.exec;


/**
 * A trivial VersionStrategy which tracks only the minimal information required to note the current state of the database: the current version.
 */
class VersionStrategy {

    private static final String DEFAULT_VALUE = "create table %s (%s varchar(32) not null unique, %s timestamp not null, %s int not null)";

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionStrategy.class);

    private static final Map<DatabaseType, String> CREATE_VERSION_TABLE_MAP;

    private String databaseName;
    private DatabaseType databaseType;

    VersionStrategy(String databaseName, DatabaseType databaseType) {
        this.databaseName = databaseName;
        this.databaseType = databaseType;
    }

    static {
        CREATE_VERSION_TABLE_MAP = new HashMap<>();
        CREATE_VERSION_TABLE_MAP.put(HSQL, "create table %s (%s varchar not null, %s datetime not null, %s int not null, constraint %2$s_unique unique (%2$s))");
        CREATE_VERSION_TABLE_MAP.put(SQL_SERVER, "create table %s (%s varchar(32) not null unique, %s datetime not null, %s int not null)");
        CREATE_VERSION_TABLE_MAP.put(CLICKHOUSE, "create table %s (%s String, %s DateTime, %s Int32) engine = Log");
        CREATE_VERSION_TABLE_MAP.put(CASSANDRA, "CREATE TABLE IF NOT EXISTS %s (%s VARCHAR, %s TIMESTAMP, %s INT , PRIMARY KEY (version))");

    }

    void createSchemaVersionTable(DatabaseType dbType) {
        LOGGER.info("Creating schema version table for {} DB", dbType);
        exec(format(CREATE_VERSION_TABLE_MAP.getOrDefault(dbType, DEFAULT_VALUE), getTableName(), "version", "applied_on", "duration"));
    }


    private String getTableName(){
        return databaseType.equals(CASSANDRA) ? databaseName + ".schema_version" : "schema_version";
    }

    boolean versionTableExists() {

        if(databaseType.equals(CASSANDRA)){
            return false; // we will attempt to create a Cassandra table every time, and the "IF NOT EXISTS" clause will be our safeguard.
        }

        try {
            countMigrations(getTableName());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    List<String> getAppliedMigrations() {
        return Base.firstColumn("select version from " + getTableName());
    }

    void recordMigration(String version, Date startTime, long duration) {
        if(databaseType.equals(CASSANDRA)){
            String useKeyspace = "USE " + this.databaseName;
            LOGGER.info("Executing: " + useKeyspace);
            exec(useKeyspace);
        }

        try{
            Object now = this.databaseType.equals(CASSANDRA) ? Instant.now() : new Timestamp(startTime.getTime());
            exec("insert into " + getTableName() + " (version, applied_on, duration) values (?, ?, ?)", version, now, Convert.toInteger(duration));
        }catch(Exception e){
            throw  new MigrationException(e);
        }
    }
}
