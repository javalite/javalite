package org.javalite.db_migrator;


import org.junit.Test;

import java.sql.SQLException;

import static java.lang.String.format;
import static java.lang.System.getProperty;

public class CreateDatabaseSpec {
    @Test
    public void createMysqlDatabase() throws Exception {
        createDatabase(format("jdbc:mysql://%s/create_database_test", getProperty("jdbc.host", "localhost")));
        createDatabase(format("jdbc:mysql://%s/create-database-test", getProperty("jdbc.host", "localhost")));
    }

    @Test
    public void createPostgresqlDatabase() throws Exception {
        createDatabase("jdbc:postgresql://localhost/create_database_test");
        createDatabase("jdbc:postgresql://localhost/create-database-test");
    }

    @Test
    public void createSqlServer2000Database() throws Exception {

        createDatabase("jdbc:jtds:sqlserver://sqlserver2000/create_database_test");
        createDatabase("jdbc:jtds:sqlserver://sqlserver2000/create-database-test");
    }

    @Test
    public void createSqlServer2005Database() throws Exception {
        createDatabase("jdbc:jtds:sqlserver://sqlserver2005/create_database_test");
        createDatabase("jdbc:jtds:sqlserver://sqlserver2005/create-database-test");
    }

    private void createDatabase(String url) throws SQLException, ClassNotFoundException {
//        final String username = "dev";
//        final String password = "dev";
//
//        new CreateDatabaseCommand(url, username, password).execute();
//
//        DataSource dataSource = DatabaseTestUtils.createDataSource(url, username, password);
//        dataSource.getConnection().close(); // Throws an exception if database is not found.
//
//        new DropDatabaseCommand(url, username, password).execute();
    }
}
