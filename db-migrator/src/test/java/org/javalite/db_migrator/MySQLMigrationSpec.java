package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.MigrationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.javalite.activejdbc.Base.count;
import static org.javalite.activejdbc.Base.firstCell;
import static org.javalite.test.jspec.JSpec.a;

public class MySQLMigrationSpec {
    private MigrationManager migrationManager;


    @Before
    public void setup() throws Exception {

        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost", "root", "p@ssw0rd");
        try {
            Base.exec("drop database mysql_migration_test");
        } catch (Exception e) {/*ignore*/}
        Base.exec("create database mysql_migration_test");
        Base.close();

        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/mysql_migration_test", "root", "p@ssw0rd");
        migrationManager = new MigrationManager("src/test/resources/test_migrations/mysql/");
    }

    @After
    public void tearDown() throws Exception {
        try {
            Base.exec("drop database mysql_migration_test");
        } catch (Exception e) {/*ignore*/}
        Base.close();
    }

    @Test
    public void shouldApplyPendingMigrations() {
        migrationManager.migrate(new MockLog());
        a(count(VersionStrategy.VERSION_TABLE)).shouldBeEqual(4);
        a(count("books")).shouldBeEqual(9);
        a(count("authors")).shouldBeEqual(2);
    }
}
