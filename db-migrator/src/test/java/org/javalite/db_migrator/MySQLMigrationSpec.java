package org.javalite.db_migrator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.javalite.db_migrator.DbUtils.*;


public class MySQLMigrationSpec {
    private MigrationManager migrationManager;


    @Before
    public void setup() throws Exception {

        openConnection("com.mysql.jdbc.Driver", "jdbc:mysql://localhost", "root", "p@ssw0rd");
        try {
            exec("drop database mysql_migration_test");
        } catch (Exception e) {/*ignore*/}
        exec("create database mysql_migration_test");
        closeConnection();

        openConnection("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/mysql_migration_test", "root", "p@ssw0rd");
        migrationManager = new MigrationManager("src/test/resources/test_migrations/mysql/");
    }

    @After
    public void tearDown() throws Exception {
        try {
            exec("drop database mysql_migration_test");
        } catch (Exception e) {/*ignore*/}
        closeConnection();
    }

    @Test
    public void shouldApplyPendingMigrations() {
        migrationManager.migrate(new MockLog(), null);
        assertEquals(countMigrations(), 4);
        assertEquals(countRows("books"), 9);
        assertEquals(countRows("authors"), 2);
    }
}
