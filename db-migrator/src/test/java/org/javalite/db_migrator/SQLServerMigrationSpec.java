package org.javalite.db_migrator;

import org.junit.Ignore;

@Ignore
public class SQLServerMigrationSpec
{
//    private MigrationManager migrationManager;
//    private SimpleJdbcTemplate jdbcTemplate;
//
//    private static final String URL = "jdbc:jtds:sqlserver://sqlserver2000/sqlserver_migration_test";
//    private static final String USERNAME = "dev";
//    private static final String PASSWORD = "dev";
//
//    public SQLServerMigrationTest() throws UnknownHostException
//    {
//        assumeThat(InetAddress.getLocalHost().getHostAddress(), startsWith("10.4.5"));
//    }
//
//    @Before
//    public void setup() throws Exception
//    {
//        new CreateDatabaseCommand(URL, USERNAME, PASSWORD).execute();
//
//        DataSource dataSource = new SimpleDriverDataSource(new Driver(), URL, USERNAME, PASSWORD);
//        migrationManager = new MigrationManager(dataSource);
//        migrationManager.setMigrationResolver(new MigrationResolver("classpath:/test_migrations/sqlserver_2000/"));
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