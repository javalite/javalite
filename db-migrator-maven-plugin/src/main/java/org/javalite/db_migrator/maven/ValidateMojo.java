package org.javalite.db_migrator.maven;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.Migration;
import org.javalite.db_migrator.MigrationManager;

import java.util.List;


/**
 * Validate current schema against available migrations.
 */
@Mojo(name = "validate")
@SuppressWarnings("unchecked")
public class ValidateMojo extends AbstractDbMigrationMojo {
    public void executeMojo() throws MojoExecutionException {
        try {
            String path = toAbsolutePath(getMigrationsPath());
            getLog().info("Validating " + getUrl() + " using migrations from " + path);
            openConnection();
            MigrationManager manager = new MigrationManager(getProject().getCompileClasspathElements(), path, getUrl());
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
