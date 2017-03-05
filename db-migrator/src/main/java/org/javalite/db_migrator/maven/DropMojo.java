package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.db_migrator.DbUtils;

import static java.lang.String.format;
import static org.javalite.db_migrator.DbUtils.*;


/**
 * @goal drop
 */
public class DropMojo extends AbstractDbMigrationMojo {
    public void executeMojo() throws MojoExecutionException {
        try {
            String dropSql = blank(getDropSql()) ? "drop database %s" : getDropSql();
            openConnection(true);
            exec(format(dropSql, DbUtils.extractDatabaseName(getUrl())));
            getLog().info("Dropped database " + getUrl());
        } finally {
            closeConnection();
        }
    }
}