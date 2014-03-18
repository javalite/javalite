package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.MigrationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.javalite.activejdbc.Base.firstCell;
import static org.javalite.test.jspec.JSpec.a;

public class HSQLMigrationSpec {
    private MigrationManager migrationManager;


    @Before
    public void setup() throws Exception {
        Base.open("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:./target/tmp/hsql-migration-test", "sa", "");
        migrationManager = new MigrationManager("src/test/resources/test_migrations/hsql/");
    }

    @After
    public void tearDown() throws Exception {
        Base.close();
    }

    @Test
    public void shouldApplyPendingMigrations() {
        migrationManager.migrate(new MockLog());
        a(firstCell("select count(version) from schema_version")).shouldBeEqual(2);
    }
}