package org.javalite.db_migrator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.javalite.db_migrator.JdbcPropertiesOverride.*;
import static org.junit.Assert.*;
import static org.javalite.db_migrator.DbUtils.*;


public class MySQLMigrationSpec {
    private MigrationManager migrationManager;


    @Before
    public void setup() throws Exception {

        openConnection(driver(), url(), user(), password());
        try {
            exec("drop database mysql_migration_test");
        } catch (Exception e) {/*ignore*/}
        exec("create database mysql_migration_test");
        closeConnection();
        openConnection(driver(), "jdbc:mysql://localhost/mysql_migration_test", user(), password());
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
