package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.DatabaseUtils;

import static java.lang.String.format;
import java.sql.SQLException;
import static org.javalite.common.Util.blank;

/**
 * @goal drop
 */
public class DropMojo extends AbstractDbMigrationMojo {
    public void executeMojo() throws MojoExecutionException {
        try {
            String dropSql = blank(getDropSql()) ? "drop database %s" : getDropSql();
            Base.open(getDriver(), DatabaseUtils.extractServerUrl(getUrl()), getUsername(), getPassword());
            Base.connection().createStatement().execute(format(dropSql, DatabaseUtils.extractDatabaseName(getUrl())));
            getLog().info("Dropped database " + getUrl());
        } catch (SQLException e) {
            getLog().warn("Failed to drop database " + getUrl() + ", " + e.getMessage());
        } finally {
            Base.close(true);
        }
    }
}