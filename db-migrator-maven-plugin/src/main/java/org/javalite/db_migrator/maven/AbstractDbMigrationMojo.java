package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.javalite.activejdbc.Base;
import org.javalite.cassandra.jdbc.CassandraJDBCDriver;
import org.javalite.common.Util;
import org.javalite.db_migrator.DatabaseType;
import org.javalite.db_migrator.DbUtils;
import org.javalite.db_migrator.MigrationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.javalite.db_migrator.DbUtils.*;

public abstract class AbstractDbMigrationMojo extends AbstractMigrationMojo {

    @Parameter(property = "basedir", required = true)
    private String basedir;

    @Parameter
    private String url;

    @Parameter
    private String driver;

    @Parameter
    private String username;

    @Parameter
    private String password;

    @Parameter
    private String environments;

    @Parameter
    private String configFile;

    @Parameter
    private String mergeProperties;

    private Properties currentMergeProperties;

    private String currentEnvironment;

    public Properties getCurrentMergeProperties() {
        return currentMergeProperties;
    }

    public String getCurrentEnvironment() {
        return currentEnvironment;
    }

    public final void execute() throws MojoExecutionException {
        Properties originMergeProperties = null;
        if (mergeProperties != null) {
            try {
                originMergeProperties = Util.readProperties(getMergeProperties());
            } catch (IOException e) {
                throw new MojoExecutionException("Load merge properties from " + mergeProperties, e);
            }
        }
        if (blank(environments)) {
            prepareCurrentMergeProperties(null, null, originMergeProperties);
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
            Set<String> environmentSet = new HashSet<>();
            for (String environment : environments.split("\\s*,\\s*")) {
                environmentSet.add(environment);
            }
            for (String environment : environmentSet) {
                getLog().info("Environment: " + environment);
                url = properties.getProperty(environment + ".url");
                driver = properties.getProperty(environment + ".driver");
                username = properties.getProperty(environment + ".username");
                password = properties.getProperty(environment + ".password");
                prepareCurrentMergeProperties(environment, environmentSet, originMergeProperties);
                currentEnvironment = environment;
                executeCurrentConfiguration();
            }
        }
    }

    private void prepareCurrentMergeProperties(String env, Set<String> environmentSet, Properties origin) {
        currentMergeProperties = new Properties();
        if (origin != null) {
            if (env == null) {
                currentMergeProperties.putAll(origin);
            } else {
                origin.entrySet().forEach(entry -> {
                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    String[] parts = key.split("\\.");
                    if (parts.length > 1 && environmentSet.contains(parts[0])) {
                        if (env.equals(parts[0])) {
                            currentMergeProperties.setProperty(key.substring(env.length() + 1), value);
                        }
                    } else {
                        currentMergeProperties.putIfAbsent(key, value);
                    }
                });
            }
        }
        currentMergeProperties.setProperty("url", url);
        currentMergeProperties.setProperty("driver", driver);
        currentMergeProperties.setProperty("username", username);
        currentMergeProperties.setProperty("password", password);
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

        if(!driver.equals(CassandraJDBCDriver.class.getName())){
            if (blank(username)) {
                throw new MojoExecutionException("No database username. Specify one in the plugin configuration.");
            }

            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new MojoExecutionException("Can't load driver class " + driver + ". Be sure to include it as a plugin dependency.");
            }
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

    public String getMergeProperties() {
        return mergeProperties;
    }

    public void setMergeProperties(String mergeProperties) {
        this.mergeProperties = mergeProperties;
    }

    /**
     * Opens a connection as specified in the URL
     */
    void openConnection(){
        openConnection(false);
    }

    /**
     * @param root open connection to root URL
     */
    void openConnection(boolean root){
        String url = root? DbUtils.extractServerUrl(getUrl()): getUrl();

        if(getDriver().equals(CassandraJDBCDriver.class.getName())){
            try{
                DriverManager.registerDriver(new CassandraJDBCDriver());
            }catch(Exception e){
                throw new MigrationException(e);
            }
        }

        Base.open(getDriver(), url, getUsername(), getPassword());

        try{ // Ignoring this is needed for the CreateMojo, since the keyspace does not exist yet
            if(DatabaseType.CASSANDRA.equals(DbUtils.databaseType(url))){
                exec("USE " + DbUtils.extractDatabaseName(url));
            }
        }catch(Exception ignore){}
    }
}
