package org.javalite.db_migrator;

import org.junit.Ignore;

@Ignore
public class PostgreSQLMigrationSpec
{
    private MigrationManager migrationManager;


    private static final String URL = "jdbc:postgresql://localhost/postgresql_migration_test";
    private static final String USERNAME = "dev";
    private static final String PASSWORD = "dev";
//
//    @Before
//    public void setup() throws Exception
//    {
//        new CreateDatabaseCommand(URL, USERNAME, PASSWORD).execute();
//
//        DataSource dataSource = new SimpleDriverDataSource(new Driver(), URL, USERNAME, PASSWORD);
//        migrationManager = new MigrationManager(dataSource);
//        migrationManager.setMigrationResolver(new MigrationResolver("classpath:/test_migrations/postgresql_8/"));
//
//        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
//    }
//
//    @After
//    public void teardown() throws Exception
//    {
//        new DropDatabaseCommand(URL, USERNAME, PASSWORD).execute();
//    }
//
//    @Test
//    public void migrateShouldApplyPendingMigrations()
//    {
//        migrationManager.migrate();
//
//        assertThat(jdbcTemplate.queryForInt("select count(version) from schema_version"), is(2));
//    }
}