package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.javalite.db_migrator.DatabaseType;
import org.javalite.db_migrator.DatabaseUtils;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.join;


public abstract class AbstractMigrationMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
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
    private String url;

    /**
     * @parameter
     */
    private String driver;

    /**
     * @parameter
     */
    private String username;

    /**
     * @parameter
     */
    private String password = "";

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

    private String databaseType;

    public final void execute() throws MojoExecutionException {
        if (blank(url) && blank(username)) {
            getLog().info("url and username not provided");
            return;
        }

        if (!getClass().equals(NewMojo.class)) {
            validateConfiguration();

            password = blank(password) ? "" : password;
            if (blank(driver))
                driver = DatabaseUtils.driverClass(url);

            databaseType = DatabaseUtils.databaseType(url).toString();
        }
        executeMojo();
    }

    public abstract void executeMojo() throws MojoExecutionException;

    protected void validateConfiguration() throws MojoExecutionException {
        if (blank(driver)) {
            throw new MojoExecutionException("No database driver. Specify one in the plugin configuration.");
        }

        if (blank(url)) {
            throw new MojoExecutionException("No database url. Specify one in the plugin configuration.");
        }

        if (blank(username)) {
            throw new MojoExecutionException("No database username. Specify one in the plugin configuration.");
        }

        try {
            if (!blank(databaseType))
                DatabaseType.valueOf(databaseType);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException(
                    "Database type of '" + databaseType + "' is invalid.  Correct values: " + join(list(DatabaseType.values()), ", "));
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Can't load driver class " + driver + ". Be sure to include it as a plugin dependency.");
        }
    }


    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }

    public String getUsername() {
        return username;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getMigrationsPath() {
        return project == null? migrationsPath: project.getBasedir() + System.getProperty("file.separator") + migrationsPath;
    }

    public String getCreateSql() {
        return createSql;
    }

    public String getDropSql() {
        return dropSql;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
}
