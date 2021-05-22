package org.javalite.db_migrator;


import org.javalite.activejdbc.Base;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.javalite.db_migrator.DbUtils.*;
import static org.javalite.test.jspec.JSpec.the;


public class HSQLMigrationSpec {
    private MigrationManager migrationManager;


    @Before
    public void setup() throws Exception {
        String url = "jdbc:hsqldb:file:./target/tmp/hsql-migration-test";
        Base.open("org.hsqldb.jdbcDriver", url, "sa", "");
        migrationManager = new MigrationManager("src/test/resources/test_migrations/hsql/", url);
    }

    @After
    public void tearDown() throws Exception {
        Base.close();
    }

    @Test
    public void shouldApplyPendingMigrations() {
        migrationManager.migrate(new MockLog(), null);
        the(countMigrations("schema_version")).shouldBeEqual(2);
    }
}