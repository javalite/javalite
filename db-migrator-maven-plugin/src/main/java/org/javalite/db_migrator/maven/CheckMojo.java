package org.javalite.db_migrator.maven;


import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.Migration;
import org.javalite.db_migrator.MigrationManager;

import java.util.List;



/**
 * Check current schema against available migrations to see if database is up to date,
 * causing the build to fail if the database is not up to date.
 * <p></p>
 *
 * @goal check
 * @phase process-test-resources
 */
public class CheckMojo extends AbstractDbMigrationMojo {
    public void executeMojo() throws MojoExecutionException {
        getLog().info("Checking " + getUrl() + " using migrations from " + getMigrationsPath());

        List<Migration> pendingMigrations;
        try {
            openConnection();
            MigrationManager manager = new MigrationManager(getMigrationsPath(), getUrl());
            pendingMigrations = manager.getPendingMigrations();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to check " + getUrl(), e);
        }finally {
            Base.close();
        }

        if (pendingMigrations.isEmpty()) return;

        getLog().warn("Pending migration(s): ");
        for (Migration migration : pendingMigrations)
            getLog().warn("Migration: " + migration.getName());

        getLog().warn("Run db-migrator:migrate to apply pending migrations.");
        throw new MojoExecutionException("Pending migration(s) exist, migrate your db and try again.");
    }
}