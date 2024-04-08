package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.javalite.common.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.javalite.db_migrator.DbUtils.countMigrations;
import static org.javalite.db_migrator.DbUtils.exec;
import static org.javalite.db_migrator.JdbcPropertiesOverride.*;
import static org.javalite.test.jspec.JSpec.the;


public class MySQLTemplatorMigrationSpec {
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



        migrationManager = new MigrationManager(new ArrayList<>(), "src/test/resources/test_migrations/mysql-templator/",
                url,
                Util.readProperties("/templator/table-names.properties"));
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
        migrationManager.migrate(null);
        the(countMigrations("schema_version")).shouldBeEqual(2);
        the(Base.count("books")).shouldBeEqual(9);
        the(Base.count("authors")).shouldBeEqual(2);
    }
}
