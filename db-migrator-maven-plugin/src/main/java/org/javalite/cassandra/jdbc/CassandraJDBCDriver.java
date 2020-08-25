package org.javalite.cassandra.jdbc;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.javalite.common.Util;

import java.io.File;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class CassandraJDBCDriver implements Driver {
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        
        /*
            jdbc:javalite-cassandra://ignored/keyspace?src/test/application.conf
         */


        String[] parts = Util.split(url, '?');
        if (parts.length != 2) {
            throw new IllegalArgumentException("The URL needs to have a format: jdbc:javalite-cassandra//ignored/keyspace?path-to-application.conf. " +
                    "However, the URL is: " + url);
        }

        File applicationConfFile = new File(parts[1]);

        if (!applicationConfFile.exists()) {
            throw new IllegalArgumentException("The file " + applicationConfFile + " must exist");
        }

        CqlSession session = CqlSession.builder()
                .withConfigLoader(DriverConfigLoader.fromFile(applicationConfFile))
                .withLocalDatacenter("datacenter1")
                .build();

        return new CassandraJDBCConnection(session);
    }


    /**
     * format: jdbc:cassandra://ignored/keyspace
     *
     * @param url
     * @return
     * @throws SQLException
     */
    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.contains("jdbc:javalite-cassandra://");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMinorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
