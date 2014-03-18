package org.javalite.db_migrator.maven;

import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.Migration;
import org.javalite.db_migrator.MigrationManager;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.List;

/**
 * Validate current schema against available migrations.
 * <p/>
 *
 * @goal validate
 */
public class ValidateMojo extends AbstractMigrationMojo {
    public void executeMojo() throws MojoExecutionException {
        getLog().info("Validating " + getUrl() + " using migrations at " + getMigrationsPath());
        try {
            Base.open(getDriver(), getUrl(), getUsername(), getPassword());
            MigrationManager manager = new MigrationManager(getMigrationsPath());
            List<Migration> pendingMigrations = manager.getPendingMigrations();

            getLog().info("Database: " + getUrl());
            getLog().info("Up-to-date: " + pendingMigrations.isEmpty());
            if (!pendingMigrations.isEmpty()) {
                getLog().info("Pending Migrations: ");
                for (Migration migration : pendingMigrations) {
                    getLog().info(migration.getName());
                }
            }else{
                getLog().info("No pending migrations found");
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to validate " + getUrl(), e);
        }finally {
            Base.close();
        }
    }
}
