package org.javalite.db_migrator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.javalite.db_migrator.DbUtils.*;

public class H2MigrationSpec {
    private MigrationManager migrationManager;

    @Before
    public void setup() throws Exception {
        openConnection("org.h2.Driver", "jdbc:h2:mem:h2-migration-test;DB_CLOSE_DELAY=-1", "sa", "");
        migrationManager = new MigrationManager("src/test/resources/test_migrations/h2/");
    }

    @After
    public void tearDown(){
        closeConnection();
    }

    @Test
    public void shouldApplyPendingMigrations() {
        migrationManager.migrate(new MockLog(), null);
        assertEquals(countMigrations(), 2);
    }
}