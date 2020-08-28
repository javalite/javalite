package org.javalite.cassandra.jdbc;

import org.javalite.db_migrator.MigrationException;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.expect;
import static org.javalite.test.jspec.JSpec.the;

public class CassandraURISpec {

    @Test
    public void shouldFailIfPrefixMissing(){
        expect(MigrationException.class, "must start with jdbc:", () -> new CassandraURI("cassandra://ignored/javalite"));
    }

    @Test
    public void shouldFailIfWrongProtocol(){
        expect(MigrationException.class, "must use protocol: cassandra", () -> new CassandraURI("jdbc:blah://ignored/javalite"));
    }

    @Test
    public void shouldFailIfHasHost(){
        expect(MigrationException.class, "host should be blank", () -> new CassandraURI("jdbc:cassandra://ignored/javalite?"));
    }

    @Test
    public void shouldFailIfQueryMissing(){
        expect(MigrationException.class, "query is missing", () -> new CassandraURI("jdbc:cassandra:///ignored/javalite"));
    }

    @Test
    public void shouldFailIfQueryMalformed(){
        expect(MigrationException.class, "query malformed", () -> new CassandraURI("jdbc:cassandra:///javalite?blah"));
    }

    @Test
    public void shouldFailIfQueryHasMoreThatOneParameter(){
        expect(MigrationException.class, "query should have just one parameter", () -> new CassandraURI("jdbc:cassandra:///javalite?blah=ew&message=hello"));
    }

    @Test
    public void shouldFailIfQueryHasWrongParameter(){
        expect(MigrationException.class, "query has a wrong parameter", () -> new CassandraURI("jdbc:cassandra:///javalite?blah=ew"));
    }

    @Test
    public void shouldSucceedIfAllParasmetersValid(){
        CassandraURI uri = new CassandraURI("jdbc:cassandra:///javalite?config_file=src/application.conf");
        the(uri.getConfigFile()).shouldEqual("src/application.conf");
        the(uri.getKeyspace()).shouldEqual("javalite");
    }

}
