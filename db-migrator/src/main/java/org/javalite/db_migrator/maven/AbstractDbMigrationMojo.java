package org.javalite.db_migrator.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.db_migrator.DbUtils;
import static org.javalite.db_migrator.DbUtils.blank;
import static org.javalite.db_migrator.DbUtils.closeQuietly;

public abstract class AbstractDbMigrationMojo extends AbstractMigrationMojo {

    /**
     * @parameter property="basedir"
     * @required
     * @readonly
     */
    private String basedir;

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
    private String password;

    /**
     * @parameter
     */
    private String environments;

    /**
     * @parameter
     */
    private String configFile;

    public final void execute() throws MojoExecutionException {
        if (blank(environments)) {
            executeCurrentConfiguration();
        } else {
            File file = blank(configFile)
                    ? new File(basedir, "database.properties")
                    : new File(configFile);
            getLog().info("Sourcing database configuration from file: " + file);
            Properties properties = new Properties();
            if (file.exists()) {
                InputStream is = null;
                try {
                    is = new FileInputStream(file);
                    properties.load(is);
                } catch (IOException e){
                    throw new MojoExecutionException("Error reading " + file + " file", e);
                } finally {
                    closeQuietly(is);
                }
            } else {
                throw new MojoExecutionException("File " + file + " not found");
            }
            for (String environment : environments.split("\\s*,\\s*")) {
                getLog().info("Environment: " + environment);
                url = properties.getProperty(environment + ".url");
                driver = properties.getProperty(environment + ".driver");
                username = properties.getProperty(environment + ".username");
                password = properties.getProperty(environment + ".password");
                executeCurrentConfiguration();
            }
        }
    }


    private void executeCurrentConfiguration() throws MojoExecutionException {
        if (blank(password)) {
            password = "";
        }
        if (blank(driver) && !blank(url)) {
            driver = DbUtils.driverClass(url);
        }

        validateConfiguration();

        executeMojo();
    }

    private void validateConfiguration() throws MojoExecutionException {
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
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Can't load driver class " + driver + ". Be sure to include it as a plugin dependency.");
        }
    }

    public abstract void executeMojo() throws MojoExecutionException;

    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEnvironments() {
        return environments;
    }

    public String getConfigFile() {
        return configFile;
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

    public void setEnvironments(String environments) {
        this.environments = environments;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    protected void openConnection(){
        openConnection(false);
    }

    /**
     * @param root open connection to root URL
     */
    protected void openConnection(boolean root){
        String url = root? DbUtils.extractServerUrl(getUrl()): getUrl();
        DbUtils.openConnection(getDriver(), url, getUsername(), getPassword());
    }
}
