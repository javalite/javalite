package org.javalite.db_migrator;

import org.apache.maven.plugin.logging.Log;
import org.javalite.activejdbc.Base;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class MigrationManager {

    private DatabaseType dbType;
    private VersionStrategy versionStrategy = new VersionStrategy();
    private MigrationResolver migrationResolver;

    public MigrationManager(String migrationLocation) throws SQLException {
        this.dbType = determineDatabaseType();
        migrationResolver = new MigrationResolver(migrationLocation);
    }

    /**
     * Validates whether the database is currently up-to-date.
     *
     * @return true if the database is up-to-date, false if it is not or is unversioned
     */
    public boolean validate() {
        return getPendingMigrations().isEmpty();
    }

    /**
     * Returns pending migrations.
     *
     * @return a sorted set of pending migrations
     */
    public List<Migration> getPendingMigrations() {
        List<String> appliedMigrations = getAppliedMigrationVersions();

        List<Migration> allMigrations = migrationResolver.resolve();
        List<Migration> pendingMigrations = new ArrayList<Migration>();
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
    public void migrate(Log log) {
        // Are migrations enabled?
        if (!isMigrationsEnabled()) {
            createSchemaVersionTable();
        }

        final Collection<Migration> pendingMigrations = getPendingMigrations();

        if (pendingMigrations.isEmpty()) {
            log.info("No new migrations are found");
            return;
        }
        log.info("Migrating database, applying " + pendingMigrations.size() + " migration(s)");
        Migration currentMigration = null;
        Base.openTransaction();
        try {
            for (Migration migration : pendingMigrations) {
                currentMigration = migration;
                log.info("Running migration " + currentMigration.getName());
                long start = System.currentTimeMillis();

                currentMigration.migrate();
                versionStrategy.recordMigration(currentMigration.getVersion(), new Date(start), (start - System.currentTimeMillis()));
                Base.commitTransaction();
            }
        } catch (Throwable e) {
            Base.rollbackTransaction();
            assert currentMigration != null;
            throw new MigrationException("Migration for version " + currentMigration.getVersion() + " failed, rolling back and terminating migration.", e);
        }
        log.info("Migrated database");
    }

    protected DatabaseType determineDatabaseType() throws SQLException {
        return DatabaseUtils.databaseType(Base.connection().getMetaData().getURL());

    }

    protected boolean isMigrationsEnabled() {
        return versionStrategy.isEnabled();
    }

    protected void createSchemaVersionTable() {
        versionStrategy.createSchemaVersionTable(dbType);
    }

    protected List<String> getAppliedMigrationVersions() {
        return versionStrategy.getAppliedMigrations();
    }
}
