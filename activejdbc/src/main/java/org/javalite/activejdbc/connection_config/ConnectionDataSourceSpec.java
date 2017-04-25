package org.javalite.activejdbc.connection_config;


import javax.sql.DataSource;

/**
 * @author Alex Saluk
 */
public class ConnectionDataSourceSpec implements ConnectionSpec {

    private final DataSource dataSource;

    public ConnectionDataSourceSpec(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}