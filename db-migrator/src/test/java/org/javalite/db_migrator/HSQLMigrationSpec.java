package org.javalite.db_migrator;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.javalite.db_migrator.DbUtils.*;


public class HSQLMigrationSpec {
    private MigrationManager migrationManager;


    @Before
    public void setup() throws Exception {
        openConnection("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:./target/tmp/hsql-migration-test", "sa", "");
        migrationManager = new MigrationManager("src/test/resources/test_migrations/hsql/");
    }

    @After
    public void tearDown() throws Exception {
        closeConnection();
    }

    @Test
    public void shouldApplyPendingMigrations() {
        migrationManager.migrate(new MockLog(), null);
        assertEquals(countMigrations(), 2);
    }
}