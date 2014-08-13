package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.MigrationManager;
import org.javalite.db_migrator.VersionStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.javalite.activejdbc.Base.count;
import static org.javalite.test.jspec.JSpec.a;

public class H2MigrationSpec {
    private MigrationManager migrationManager;

    @Before
    public void setup() throws Exception {
        Base.open("org.h2.Driver", "jdbc:h2:mem:h2-migration-test;DB_CLOSE_DELAY=-1", "sa", "");
        migrationManager = new MigrationManager("src/test/resources/test_migrations/h2/");
    }

    @After
    public void tearDown(){
        Base.close();
    }

    @Test
    public void shouldApplyPendingMigrations() {
        migrationManager.migrate(new MockLog(), null);
        a(count(VersionStrategy.VERSION_TABLE)).shouldBeEqual(2);
    }
}