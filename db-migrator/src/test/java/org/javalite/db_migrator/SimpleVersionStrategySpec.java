package org.javalite.db_migrator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.javalite.db_migrator.DbUtils.*;

public class SimpleVersionStrategySpec {

    private VersionStrategy strategy;

    @Before
    public void setup() {
        strategy = new VersionStrategy();
        openConnection("org.h2.Driver", "jdbc:h2:mem:h2-migration-test;DB_CLOSE_DELAY=-1", "sa", "");
        try {exec("drop table " + VersionStrategy.VERSION_TABLE);} catch (Exception e) {/* ignore*/}
    }

    @After
    public void tearDown() {
        closeConnection();
    }

    @Test
    public void shouldCreateSchemaVersionTable() throws SQLException {
        strategy.createSchemaVersionTable(DatabaseType.H2);
        assertEquals(countMigrations(), 0);
    }

    @Test
    public void shouldBeEmptyIfNoMigrationExecuted() throws SQLException {
        strategy.createSchemaVersionTable(DatabaseType.H2);
        List<String> migrations = strategy.getAppliedMigrations();
        assertEquals(migrations.size(), 0);
    }

    @Test
    public void shouldRecordMigrations() throws SQLException {
        String v1 = "20080718214030";
        String v2 = "20080718214530";

        strategy.createSchemaVersionTable(DatabaseType.H2);
        strategy.recordMigration(v1, new Date(), 768);

        assertEquals(countMigrations(), 1);

        strategy.recordMigration(v2, new Date(), 231);
        assertEquals(countMigrations(), 2);

        List<String> appliedMigrations = strategy.getAppliedMigrations();
        assertEquals(appliedMigrations.size(), 2);
        assertEquals(appliedMigrations.get(0), v1);
        assertEquals(appliedMigrations.get(1), v2);
    }
}
