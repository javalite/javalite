package org.javalite.activejdbc.connection_config;


import javax.sql.DataSource;

/**
 * @author Alex Saluk
 */
public class ConnectionDataSourceConfig extends ConnectionConfig {

    private final DataSource dataSource;

    public ConnectionDataSourceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}