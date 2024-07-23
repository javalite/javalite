package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.javalite.db_migrator.DbUtils.countMigrations;
import static org.javalite.test.jspec.JSpec.the;

public class ClassPathMigrationsSpec {
    private MigrationManager migrationManager;

    @Before
    public void setup() throws Exception {
        Base.open("org.h2.Driver", "jdbc:h2:mem:h2-migration-test;DB_CLOSE_DELAY=-1" , "sa", "");
        migrationManager = new MigrationManager(new ArrayList<>(),  "/db_migrations/", null);
    }

    @After
    public void tearDown(){
        Base.close();
    }

    @Test
    public void shouldApplyPendingMigrations() {
        migrationManager.migrate();
        the(countMigrations("schema_version")).shouldBeEqual(2);
    }
}
