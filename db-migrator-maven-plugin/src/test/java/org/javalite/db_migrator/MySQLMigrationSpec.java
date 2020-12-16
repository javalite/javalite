package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.javalite.common.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.javalite.db_migrator.JdbcPropertiesOverride.*;
import static org.javalite.test.jspec.JSpec.the;
import static org.junit.Assert.*;
import static org.javalite.db_migrator.DbUtils.*;


public class MySQLMigrationSpec {
    private MigrationManager migrationManager;
    private final String databaseName = "mysql_migration_test";

    @Before
    public void setup() throws Exception {

        Base.open(driver(), url(), user(), password());
        try {
            exec("drop database " + databaseName);
        } catch (Exception e) {/*ignore*/}
        exec("create database " + databaseName);
        Base.close();

        String url = url() + "/" + databaseName;
        Base.open(driver(), url, user(), password());
        migrationManager = new MigrationManager("src/test/resources/test_migrations/mysql/", url);
    }

    @After
    public void tearDown() {
        try {
            exec("drop database " + databaseName);
        } catch (Exception e) {/*ignore*/}
        Base.close();
    }

    @Test
    public void shouldApplyPendingMigrations() {
        migrationManager.migrate(new MockLog(), null);
        assertEquals(countMigrations("schema_version"), 4);
        the(Base.count("books")).shouldBeEqual(9);
        the(Base.count("authors")).shouldBeEqual(2);
    }
}
