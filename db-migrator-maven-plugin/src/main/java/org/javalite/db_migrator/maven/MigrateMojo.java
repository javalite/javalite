package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.javalite.activejdbc.Base;
import org.javalite.common.Util;
import org.javalite.db_migrator.MigrationManager;

import java.io.IOException;
import java.sql.SQLException;


/**
 * Migrate to latest schema version.
 */
@Mojo(name = "migrate", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MigrateMojo extends AbstractDbMigrationMojo {

    public void executeMojo() throws MojoExecutionException {
        getLog().info("Migrating " + getUrl() + " using migrations at " + getMigrationsPath());
        try {
            openConnection();
            new MigrationManager(getProject(), getMigrationsPath(), getUrl(),
                                getMergeProperties() == null ? null : Util.readProperties(getMergeProperties())).migrate(getLog(), getEncoding());
        } catch(SQLException | IOException e){
            throw new MojoExecutionException("Failed to migrate database " + getUrl(), e);
        } finally {
            Base.close();
        }
    }
}
