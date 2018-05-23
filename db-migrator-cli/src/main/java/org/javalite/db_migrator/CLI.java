package org.javalite.db_migrator;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.db_migrator.maven.MigrateMojo;

/**
 * @author igor on 5/23/18.
 */
public class CLI {

    public static void main(String[] args) throws MojoExecutionException {

        //this just works:
        MigrateMojo mojo = new MigrateMojo();
        mojo.setConfigFile("/home/igor/projects/javalite/activejdbc/db-migrator-integration-test/src/test/project/test-project-properties/database.properties");
        mojo.setMigrationsPath("/home/igor/projects/javalite/activejdbc/db-migrator-integration-test/src/test/project/test-project-properties/src/migrations");
        mojo.setEnvironments("development");
        mojo.execute();
    }
}
