package org.javalite.cassandra.jdbc;

import org.javalite.common.Util;
import org.javalite.db_migrator.MigrationException;

import java.net.URI;
import java.net.URISyntaxException;

import static org.javalite.common.Util.blank;

public class CassandraURI {

    private static final String JDBC_PREFIX = "jdbc:";
    private final String keyspace, configFile;


    public CassandraURI(String uriString) {

        //jdbc:cassandra:///imi_wallet?config_file=src/main/cassandra-config/application-development.conf
        try {

            if (!uriString.startsWith(JDBC_PREFIX)) {
                throw new MigrationException("must start with jdbc:");
            }

            URI uri = new URI(uriString.substring(5).replaceAll("\\\\", "/"));
            if (!"cassandra".equalsIgnoreCase(uri.getScheme())) {
                throw new MigrationException("must use protocol: cassandra");
            }

            if (!blank(uri.getHost())) {
                throw new MigrationException("host should be blank");
            }

            String query = uri.getQuery();
            if (blank(query)) {
                throw new MigrationException("query is missing");
            }

            if (!query.contains("=")) {
                throw new MigrationException("query malformed");
            }

            if (query.indexOf("=") != query.lastIndexOf("=")) {
                throw new MigrationException("query should have just one parameter");
            }

            String[] parts = Util.split(query, '=');

            if (!parts[0].equals("config_file")) {
                throw new MigrationException("query has a wrong parameter");
            }
            this.keyspace = uri.getRawPath().substring(1);
            this.configFile = parts[1];

        } catch (URISyntaxException e) {
            throw new MigrationException(e);
        }


    }


    public String getKeyspace() {
        return keyspace;
    }


    public String getConfigFile() {

        return configFile;
    }

}
