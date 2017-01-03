package org.javalite.activejdbc.connection_config;


/**
 *
 * @author Max Artyukhov
 */
public class ConnectionSpecWrapper {

    private String environment;
    private String dbName = "default";
    private boolean testing;
    private ConnectionSpec connectionSpec;

    public ConnectionSpec getConnectionSpec() {
        return connectionSpec;
    }

    void setConnectionSpec(ConnectionSpec connectionSpec) {
        this.connectionSpec = connectionSpec;
    }

    public String getDbName() {
        return dbName;
    }

    void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getEnvironment() {
        return environment;
    }

    void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isTesting() {
        return testing;
    }

    void setTesting(boolean testing) {
        this.testing = testing;
    }

}