package org.javalite.db_migrator.maven;

import java.sql.SQLException;
import org.apache.maven.plugin.*;
import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.MigrationManager;

/**
 * Migrate to latest schema version.
 * <p/>
 *
 * @goal migrate
 */
public class MigrateMojo extends AbstractDbMigrationMojo {

    public void executeMojo() throws MojoExecutionException {
        getLog().info("Migrating " + getUrl() + " using migrations at " + getMigrationsPath());
        try {
            Base.open(getDriver(), getUrl(), getUsername(), getPassword());
            new MigrationManager(getMigrationsPath()).migrate(getLog(), getEncoding());
        } catch(SQLException e){
            throw new MojoExecutionException("Failed to migrate database " + getUrl(), e);
        } finally {
            Base.close(true);
        }
    }
}
