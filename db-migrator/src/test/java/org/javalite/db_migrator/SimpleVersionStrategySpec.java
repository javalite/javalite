package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.javalite.db_migrator.DbUtils.*;
import static org.javalite.test.jspec.JSpec.the;

public class SimpleVersionStrategySpec {

    private VersionStrategy strategy;

    @Before
    public void setup() {
        strategy = new VersionStrategy("unused", DatabaseType.H2);
        Base.open("org.h2.Driver", "jdbc:h2:mem:h2-migration-test;DB_CLOSE_DELAY=-1", "sa", "");
        try {exec("drop table schema_version");} catch (Exception e) {/* ignore*/}
    }

    @After
    public void tearDown() {
        Base.close();
    }

    @Test
    public void shouldCreateSchemaVersionTable() throws SQLException {
        strategy.createSchemaVersionTable(DatabaseType.H2);
        the(countMigrations("schema_version")).shouldBeEqual(0);
    }

    @Test
    public void shouldBeEmptyIfNoMigrationExecuted() throws SQLException {
        strategy.createSchemaVersionTable(DatabaseType.H2);
        List<String> migrations = strategy.getAppliedMigrations();
        the(migrations.size()).shouldBeEqual(0);
    }

    @Test
    public void shouldRecordMigrations() throws SQLException {
        String v1 = "20080718214030";
        String v2 = "20080718214530";

        strategy.createSchemaVersionTable(DatabaseType.H2);
        strategy.recordMigration(v1, new Date(), 768);

        the(countMigrations("schema_version")).shouldBeEqual(1);

        strategy.recordMigration(v2, new Date(), 231);
        the(countMigrations("schema_version")).shouldBeEqual(2);

        List<String> appliedMigrations = strategy.getAppliedMigrations();
        the(appliedMigrations.size()).shouldBeEqual(2);
        the(appliedMigrations.get(0)).shouldBeEqual(v1);
        the(appliedMigrations.get(1)).shouldBeEqual(v2);
    }
}
