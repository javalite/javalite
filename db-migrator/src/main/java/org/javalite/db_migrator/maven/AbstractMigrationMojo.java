package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

public abstract class AbstractMigrationMojo extends AbstractMojo {

    /**
     * @parameter property="project"
     * @required
     */
    protected MavenProject project;

    /**
     * @parameter
     */
    private String encoding;

    /**
     * @parameter
     */
    private String migrationsPath = "src/migrations/";

    /**
     * @parameter
     */
    private String createSql;
    /**
     * @parameter
     */
    private String dropSql;

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
