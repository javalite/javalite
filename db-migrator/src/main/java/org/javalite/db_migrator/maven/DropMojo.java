package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.DatabaseUtils;

import static java.lang.String.format;
import static org.javalite.common.Util.blank;

/**
 * @goal drop
 */
public class DropMojo extends AbstractMigrationMojo {
    public void executeMojo() throws MojoExecutionException {

        try {
            String dropSql = blank(getDropSql()) ? "drop database %s" : getDropSql();
            Base.open(getDriver(), DatabaseUtils.extractServerUrl(getUrl()), getUsername(), getPassword());
            Base.connection().createStatement().execute(format(dropSql, DatabaseUtils.extractDatabaseName(getUrl())));
            getLog().info("Dropped database " + getUrl());
        } catch (Exception e) {
            getLog().warn("Failed to drop database " + getUrl() + ", " + e.getMessage());
        }finally {
            Base.close();
        }
    }
}