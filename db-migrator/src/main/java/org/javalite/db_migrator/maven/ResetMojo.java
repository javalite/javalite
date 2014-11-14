package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;


/**
 * @goal reset
 */
public class ResetMojo extends AbstractDbMigrationMojo {
    public void executeMojo() throws MojoExecutionException {
        getLog().info("Resetting database " + getUrl());

        try {
            DropMojo dropMojo = new DropMojo();
            set(dropMojo);
            CreateMojo createMojo = new CreateMojo();
            set(createMojo);
            MigrateMojo migrateMojo = new MigrateMojo();
            set(migrateMojo);

            dropMojo.executeMojo();
            createMojo.executeMojo();
            migrateMojo.executeMojo();
        } catch (MojoExecutionException e) {
            throw new MojoExecutionException("Failed to reset database " + getUrl(), e);
        }
    }

    private void set(AbstractDbMigrationMojo mojo) {
        mojo.setLog(getLog());
        mojo.setUrl(getUrl());
        mojo.setUsername(getUsername());
        mojo.setPassword(getPassword());
        mojo.setDropSql(getDropSql());
        mojo.setCreateSql(getCreateSql());
        mojo.setMigrationsPath(getMigrationsPath());
        mojo.setDriver(getDriver());
    }
}