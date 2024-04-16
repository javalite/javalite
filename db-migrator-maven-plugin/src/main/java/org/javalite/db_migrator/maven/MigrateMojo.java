package org.javalite.db_migrator.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.MigrationManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


/**
 * Migrate to latest schema version.
 */
@Mojo(name = "migrate", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MigrateMojo extends AbstractDbMigrationMojo {
    @SuppressWarnings("unchecked")
    public void executeMojo() throws MojoExecutionException {
        try {
            String path = toAbsolutePath(getMigrationsPath());
            getLog().info("Migrating " + getUrl() + " using migrations at " + path);
            openConnection();
            new MigrationManager(getProject().getCompileClasspathElements(), new File(path), getCurrentMergeProperties() == null ? null : getCurrentMergeProperties()).migrate();
        } catch(Exception e){
            throw new MojoExecutionException("Failed to migrate database " + getUrl(), e);
        } finally {
            Base.close();
        }
    }
}
