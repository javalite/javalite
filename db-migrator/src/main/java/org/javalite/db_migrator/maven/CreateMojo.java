package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.db_migrator.DbUtils;
import org.javalite.db_migrator.MigrationManager;

import static java.lang.String.format;
import static org.javalite.db_migrator.DbUtils.*;


/**
 * @goal create
 */
public class CreateMojo extends AbstractDbMigrationMojo {

    public void executeMojo() throws MojoExecutionException {

        try {

            String createSql = blank(getCreateSql()) ? "create database %s" : getCreateSql();
            String databaseName = DbUtils.extractDatabaseName(getUrl());
            switch (DbUtils.databaseType(getUrl())) {
                case MYSQL:
                    break;
                case SQL_SERVER:
                    break;
                case POSTGRESQL:
                    databaseName = "\"" + databaseName + "\"";
                    break;
            }

            openConnection(true);
            exec(format(createSql, databaseName));
            getLog().info("Created database " + getUrl());
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create database: " + getUrl(), e);
        } finally {
            closeConnection();
        }

        try{
            openConnection();
            new MigrationManager(getMigrationsPath()).createSchemaVersionTable();

        }catch(Exception e){
            throw  new MojoExecutionException("failed to create SCHEMA_VERSION table", e);
        }finally {
            closeConnection();
        }
    }
}
