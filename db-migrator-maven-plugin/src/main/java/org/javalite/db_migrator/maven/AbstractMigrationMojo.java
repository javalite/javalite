package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractMigrationMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter
    private String encoding;

    @Parameter(defaultValue = "src/migrations/")
    private String migrationsPath;

    @Parameter
    private String createSql;

    @Parameter
    private String dropSql;

    public MavenProject getProject() {
        return project;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getMigrationsPath() {
        return project == null? migrationsPath: project.getBasedir()
                + System.getProperty("file.separator") + migrationsPath;
    }

    public String getCreateSql() {
        return createSql;
    }

    public String getDropSql() {
        return dropSql;
    }

    public void setMigrationsPath(String migrationsPath) {
        this.migrationsPath = migrationsPath;
    }

    public void setCreateSql(String createSql) {
        this.createSql = createSql;
    }

    public void setDropSql(String dropSql) {
        this.dropSql = dropSql;
    }
}
