package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.DatabaseUtils;

import static java.lang.String.format;
import static org.javalite.common.Util.blank;

/**
 * @goal create
 */
public class CreateMojo extends AbstractMigrationMojo {

    public void executeMojo() throws MojoExecutionException {

        try {

            String createSql = blank(getCreateSql()) ? "create database %s" : getCreateSql();
            String databaseName = DatabaseUtils.extractDatabaseName(getUrl());
            switch (DatabaseUtils.databaseType(getUrl())) {
                case MYSQL:
                    break;
                case SQL_SERVER:
                    break;
                case POSTGRESQL:
                    databaseName = "\"" + databaseName + "\"";
                    break;
            }

            Base.open(getDriver(), DatabaseUtils.extractServerUrl(getUrl()), getUsername(), getPassword());
            Base.exec(format(createSql, databaseName));
            getLog().info("Created database " + getUrl());
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create database: " + getUrl(), e);
        } finally {
            Base.close(true);
        }
    }
}
