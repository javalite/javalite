package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.javalite.cassandra.jdbc.CassandraJDBCConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.javalite.db_migrator.DbUtils.databaseType;


public class MigrationManager {

    static final Logger MIGRATION_LOGGER = LoggerFactory.getLogger(MigrationManager.class);

    private DatabaseType dbType;
    private VersionStrategy versionStrategy;
    private MigrationResolver migrationResolver;


    public MigrationManager(List<String> paths, File migrationLocation)  {
        this(paths, migrationLocation, null);
    }

    public MigrationManager(List<String> paths, File migrationsLocation, Properties mergeProperties) {

        try{
            this.dbType = determineDatabaseType();
            migrationResolver = new MigrationResolver(paths, migrationsLocation, mergeProperties);
            String databaseName;

            databaseName = DbUtils.extractDatabaseName(Base.connection().getMetaData().getURL());
            versionStrategy = new VersionStrategy(databaseName, dbType);
        }catch(Exception e){
            throw new MigrationException(e);
        }
    }

    public MigrationManager(List<String> paths, String classpathMigrationsLocation, Properties mergeProperties) throws SQLException {
        this.dbType = determineDatabaseType();
        migrationResolver = new MigrationResolver(paths, classpathMigrationsLocation, mergeProperties);
        String databaseName;

        databaseName = DbUtils.extractDatabaseName(Base.connection().getMetaData().getURL());
        versionStrategy = new VersionStrategy(databaseName, dbType);
    }

    /**
     * To be used in web apps that package migration files on the classpath
     *
     * @param classpathMigrationsLocation location of the migrations on teh classpath (not on file system).
     * @throws SQLException
     */
    public MigrationManager(String classpathMigrationsLocation) {

        try{
            this.dbType = determineDatabaseType();
            migrationResolver = new MigrationResolver(new ArrayList<>(), classpathMigrationsLocation, null);
            String databaseName;

            databaseName = DbUtils.extractDatabaseName(Base.connection().getMetaData().getURL());
            versionStrategy = new VersionStrategy(databaseName, dbType);
        }catch(Exception e){
            throw new MigrationException(e);
        }
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
    public void migrate() {

        createSchemaVersionTableIfDoesNotExist();

        final Collection<Migration> pendingMigrations = getPendingMigrations();

        if (pendingMigrations.isEmpty()) {
            MIGRATION_LOGGER.info("No new migrations are found");
            return;
        }
        MIGRATION_LOGGER.info("Migrating database, applying " + pendingMigrations.size() + " migration(s)");
        Migration currentMigration = null;

        try {
            Base.connection().setAutoCommit(false);
            for (Migration migration : pendingMigrations) {
                currentMigration = migration;
                MIGRATION_LOGGER.info("Running migration " + currentMigration.getFileName());
                long start = System.currentTimeMillis();

                currentMigration.migrate();
                versionStrategy.recordMigration(currentMigration.getVersion(), new Date(start), (start - System.currentTimeMillis()));
                Base.connection().commit();
            }
        } catch (Exception e) {
            try{Base.connection().rollback();}catch(Exception ex){throw new MigrationException(e);}
            assert currentMigration != null;
            throw new MigrationException("Migration for version " + currentMigration.getVersion() + " failed, rolling back and terminating migration.", e);
        }
        MIGRATION_LOGGER.info("Migrated database");
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
