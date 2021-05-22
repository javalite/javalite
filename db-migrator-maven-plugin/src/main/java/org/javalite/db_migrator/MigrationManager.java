package org.javalite.db_migrator;

import org.apache.maven.plugin.logging.Log;
import org.javalite.activejdbc.Base;
import org.javalite.cassandra.jdbc.CassandraJDBCConnection;
import org.javalite.common.Templator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.javalite.db_migrator.DbUtils.databaseType;

public class MigrationManager {

    private DatabaseType dbType;
    private VersionStrategy versionStrategy;
    private MigrationResolver migrationResolver;


    public MigrationManager(String migrationLocation, String url) throws SQLException {
        this(migrationLocation, url, null);
    }

    public MigrationManager(String migrationLocation, String url, Properties mergeProperties) throws SQLException {
        this.dbType = determineDatabaseType();
        migrationResolver = new MigrationResolver(migrationLocation, mergeProperties);
        String databaseName;
        if(url != null){
            databaseName = DbUtils.extractDatabaseName(url);
        }else{
            throw new IllegalArgumentException("URL cannot be null");
        }
        versionStrategy = new VersionStrategy(databaseName, dbType);
    }

    /**
     * Returns pending migrations.
     *
     * @return a sorted set of pending migrations
     */
    public List<Migration> getPendingMigrations() {
        List<String> appliedMigrations = getAppliedMigrationVersions();

        List<Migration> allMigrations = migrationResolver.resolve();
        List<Migration> pendingMigrations = new ArrayList<>();
        for (Migration migration : allMigrations) {
            if(!appliedMigrations.contains(migration.getVersion())){
                pendingMigrations.add(migration);
            }
        }
        return pendingMigrations;
    }

    /**
     * Migrates the database to the latest version, enabling migrations if necessary.
     */
    public void migrate(Log log, String encoding) {

        createSchemaVersionTableIfDoesNotExist();

        final Collection<Migration> pendingMigrations = getPendingMigrations();

        if (pendingMigrations.isEmpty()) {
            log.info("No new migrations are found");
            return;
        }
        log.info("Migrating database, applying " + pendingMigrations.size() + " migration(s)");
        Migration currentMigration = null;

        try {
            Base.connection().setAutoCommit(false);
            for (Migration migration : pendingMigrations) {
                currentMigration = migration;
                log.info("Running migration " + currentMigration.getName());
                long start = System.currentTimeMillis();

                currentMigration.migrate(encoding);
                versionStrategy.recordMigration(currentMigration.getVersion(), new Date(start), (start - System.currentTimeMillis()));
                Base.connection().commit();
            }
        } catch (Exception e) {
            try{Base.connection().rollback();}catch(Exception ex){throw new MigrationException(e);}
            assert currentMigration != null;
            throw new MigrationException("Migration for version " + currentMigration.getVersion() + " failed, rolling back and terminating migration.", e);
        }
        log.info("Migrated database");
    }

    private DatabaseType determineDatabaseType() throws SQLException {
        Connection connection =  Base.connection();
        return connection instanceof CassandraJDBCConnection ? DatabaseType.CASSANDRA
                : databaseType(Base.connection().getMetaData().getURL());
    }

    private boolean versionTableExists() {
        return versionStrategy.versionTableExists();
    }

    public void createSchemaVersionTableIfDoesNotExist() {
        if (!versionTableExists()) {
            versionStrategy.createSchemaVersionTable(dbType);
        }
    }

    private List<String> getAppliedMigrationVersions() {
        return versionStrategy.getAppliedMigrations();
    }
}
