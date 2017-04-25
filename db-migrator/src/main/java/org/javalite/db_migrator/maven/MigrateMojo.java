package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.db_migrator.MigrationManager;

import java.sql.SQLException;

import static org.javalite.db_migrator.DbUtils.closeConnection;

/**
 * Migrate to latest schema version.
 * <p></p>
 *
 * @goal migrate
 */
public class MigrateMojo extends AbstractDbMigrationMojo {

    public void executeMojo() throws MojoExecutionException {
        getLog().info("Migrating " + getUrl() + " using migrations at " + getMigrationsPath());
        try {
            openConnection();
            new MigrationManager(getMigrationsPath()).migrate(getLog(), getEncoding());
        } catch(SQLException e){
            throw new MojoExecutionException("Failed to migrate database " + getUrl(), e);
        } finally {
            closeConnection();
        }
    }
}
