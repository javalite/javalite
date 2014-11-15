package org.javalite.db_migrator.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.common.Util;

import static org.javalite.common.Util.*;
import org.javalite.db_migrator.DatabaseUtils;

public abstract class AbstractDbMigrationMojo extends AbstractMigrationMojo {

    /**
     * @parameter property="session"
     * @required
     * @readonly
     */
    private MavenSession session;    
    
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

    public final void execute() throws MojoExecutionException {
        if (blank(environments)) {
            executeCurrentConfiguration();
        } else {
            Properties properties = new Properties();
            // looks for database.properties file in basedir
            //TODO: parameter with properties file location
            File file = new File(session.getExecutionRootDirectory(), "database.properties");
            if (file.exists()) {
                InputStream is = null;
                try {
                    is = new FileInputStream(file);
                    properties.load(is);
                } catch (IOException e){
                    throw new MojoExecutionException("Error reading database.properties file", e);
                } finally {
                    Util.close(is);
                }
            }
            for (String environment : environments.split("\\s*,\\s*")) {
                getLog().info("Environment: " + environment);
                url = properties.getProperty(environment + ".jdbc.url");
                driver = properties.getProperty(environment + ".jdbc.driver");
                username = properties.getProperty(environment + ".jdbc.username");
                password = properties.getProperty(environment + ".jdbc.password");
                executeCurrentConfiguration();
            }
        }
    }
    
    private void executeCurrentConfiguration() throws MojoExecutionException {
        if (blank(password)) {
            password = "";
        }
        if (blank(driver) && !blank(url)) {
            driver = DatabaseUtils.driverClass(url);
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
}
