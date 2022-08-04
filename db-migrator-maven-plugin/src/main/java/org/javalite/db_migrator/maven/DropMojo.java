package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.DbUtils;

import static java.lang.String.format;
import static org.javalite.db_migrator.DbUtils.blank;
import static org.javalite.db_migrator.DbUtils.exec;


@Mojo(name = "drop")
public class DropMojo extends AbstractDbMigrationMojo {
    public void executeMojo() throws MojoExecutionException {
        try {
            String dropSql = blank(getDropSql()) ? "drop database %s" : getDropSql();
            openConnection(true);

            String databaseName = DbUtils.extractDatabaseName(getUrl());
            if(databaseExists(databaseName)){
                exec(format(dropSql, databaseName));
                getLog().info("Dropped database " + getUrl());
            }else {
                getLog().warn("The database '" + databaseName + "' does not exist, cannot drop.");
            }
        }catch (Exception e){
            getLog().error("Failed to drop the database", e);
        }finally {
            Base.close();
        }
    }
}