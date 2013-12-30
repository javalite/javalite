package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.javalite.activejdbc.Base.count;
import static org.javalite.activejdbc.Base.firstCell;
import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

public class SimpleVersionStrategySpec {

    private VersionStrategy strategy;

    @Before
    public void setup() {
        strategy = new VersionStrategy();
        Base.open("org.h2.Driver", "jdbc:h2:mem:h2-migration-test;DB_CLOSE_DELAY=-1", "sa", "");
        try{Base.exec("drop table " + VersionStrategy.VERSION_TABLE);}catch(Exception e){}
    }

    @After
    public void tearDown() {
        Base.close();
    }

    @Test
    public void shouldCreateSchemaVersionTable() throws SQLException {
        strategy.createSchemaVersionTable(DatabaseType.H2);
        a(count(VersionStrategy.VERSION_TABLE)).shouldBeEqual(0);
    }

    @Test
    public void shouldBeEmptyIfNoMigrationExecuted() throws SQLException {
        List<String> migrations = strategy.getAppliedMigrations();
        a(migrations.size()).shouldBeEqual(0);
    }

    @Test
    public void shouldRecordMigrations() throws SQLException {
        String v1 = "20080718214030";
        String v2 = "20080718214530";

        strategy.createSchemaVersionTable(DatabaseType.H2);
        strategy.recordMigration(v1, new Date(), 768);

        a(count(VersionStrategy.VERSION_TABLE)).shouldBeEqual(1);
        a(firstCell("select " +VersionStrategy.VERSION_COLUMN + " from " + VersionStrategy.VERSION_TABLE)).shouldBeEqual(v1);

        strategy.recordMigration(v2, new Date(), 231);
        a(count(VersionStrategy.VERSION_TABLE)).shouldBeEqual(2);

        List<String> appliedMigrations = strategy.getAppliedMigrations();
        the(appliedMigrations.size()).shouldBeEqual(2);
        the(appliedMigrations).shouldContain(v1);
        the(appliedMigrations).shouldContain(v2);
    }
}
