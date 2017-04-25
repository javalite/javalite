package org.javalite.db_migrator;

import org.junit.Test;

public class DropDatabaseSpec {
    @Test
    public void dropMysqlDatabase() throws Exception {
        dropDatabase("jdbc:mysql://%s/drop_database_test");
    }

    @Test
    public void dropPostgresqlDatabase() throws Exception {
        dropDatabase("jdbc:postgresql://%s/drop_database_test");
    }

    @Test
    public void dropSqlServer2000Database() throws Exception {
        dropDatabase("jdbc:jtds:sqlserver://sqlserver2000/drop_database_test");
    }

    @Test
    public void dropSqlServer2005Database() throws Exception {
        dropDatabase("jdbc:jtds:sqlserver://sqlserver2005/drop_database_test");
    }

    void dropDatabase(String url) throws Exception {
//        final String username = "root";
//        final String password = "p@ssw0rd";
//
//        new CreateDatabaseCommand(url, username, password).execute();
//        new DropDatabaseCommand(url, username, password).execute();
//
//        try {
//            DataSource dataSource = DatabaseTestUtils.createDataSource(url, username, password);
//            dataSource.getConnection().close(); // Throws an exception if database is not found.
//            fail("Exception should have been thrown indicating that the database does not exist.");
//        } catch (Exception ignored) {
//        }
    }
}