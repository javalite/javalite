package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getMigrationsPath() {
        return migrationsPath;
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

    public String toAbsolutePath(String path) throws FileNotFoundException {
        return toAbsolutePath(path, false);
    }

    public String toAbsolutePath(String path, boolean suppressNotExists) throws FileNotFoundException {
        File f = new File(path);
        if (f.exists()) {
            f = Path.of(f.getAbsolutePath()).normalize().toFile();
        } else {
            if (project != null) {
                f = Path.of(project.getBasedir().getAbsolutePath(), path).normalize().toFile();
            }
            if (!f.exists() && !suppressNotExists) {
                throw new FileNotFoundException(f.getAbsolutePath());
            }
        }
        return f.getAbsolutePath();
    }
}
