package org.javalite.db_migrator.maven;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.Migration;
import org.javalite.db_migrator.MigrationManager;

import java.io.File;
import java.util.List;



/**
 * Check current schema against available migrations to see if database is up to date,
 * causing the build to fail if the database is not up to date.
 * <p></p>
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
@SuppressWarnings("unchecked")
public class CheckMojo extends AbstractDbMigrationMojo {
    public void executeMojo() throws MojoExecutionException {
        List<Migration> pendingMigrations;
        try {
            String path = toAbsolutePath(getMigrationsPath());
            getLog().info("Checking " + getUrl() + " using migrations from " + path);
            openConnection();
            MigrationManager manager = new MigrationManager(getProject().getCompileClasspathElements(), new File(path));
            pendingMigrations = manager.getPendingMigrations();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to check " + getUrl(), e);
        }finally {
            Base.close();
        }

        if (pendingMigrations.isEmpty()) return;

        getLog().warn("Pending migration(s): ");
        for (Migration migration : pendingMigrations)
            getLog().warn("Migration: " + migration.getFileName());

        getLog().warn("Run db-migrator:migrate to apply pending migrations.");
        throw new MojoExecutionException("Pending migration(s) exist, migrate your db and try again.");
    }
}