package org.javalite.cassandra.jdbc;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;

import java.io.File;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class CassandraJDBCDriver implements Driver {
    @Override
    public Connection connect(String urlString, Properties info) throws SQLException {
        CassandraURI cassandraURI;

         try{
             cassandraURI = new CassandraURI(urlString);
         }catch(Exception e){
             return null;
         }

        File applicationConfFile = new File(cassandraURI.getConfigFile());

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
    public boolean acceptsURL(String url) throws SQLException{
        try{
            new CassandraURI(url);
            return true;
        }catch(Exception e){
            return false;
        }
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
