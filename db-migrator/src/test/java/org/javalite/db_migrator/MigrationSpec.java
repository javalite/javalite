package org.javalite.db_migrator;

import org.javalite.db_migrator.mock.MockConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.javalite.common.Util.readResource;
import static org.javalite.db_migrator.DbUtils.attach;
import static org.javalite.db_migrator.DbUtils.detach;
import static org.javalite.test.jspec.JSpec.a;


public class MigrationSpec {

    MockConnection connection;

    @Before
    public void before() {
        connection = new MockConnection();
        attach(connection);
    }

    @After
    public void after(){
        detach();
    }



    @Test
    public void shouldBatchSimpleCommands() throws Exception {

        Migration m = new Migration("123", new File("src/test/resources/sql/simple.sql"));

        m.migrate(null);
        List statements = getStatements();

        a(statements.size()).shouldBeEqual(2);
        shouldBeEqual(statements.get(0), "/expected/create_users.sql");
        shouldBeEqual(statements.get(1), "/expected/alter_users.sql");
    }

    private List getStatements() {
        return connection.getExecutedStatements();
    }

    @Test
    public void shouldHandleComplexCommands() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/complex.sql"));
        m.migrate(null);
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(1);
        shouldBeEqual(statements.get(0), "/expected/dav_file.sql");
    }

    @Test
    public void shouldBatchMySQLFunctionsAndProcedures() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/stored-procedure-mysql.sql"));
        m.migrate(null);
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(3);
        a(statements.get(0)).shouldBeEqual("CREATE FUNCTION hello (s CHAR(20)) RETURNS CHAR(50) DETERMINISTIC RETURN CONCAT('Hello, ',s,'!')");
        shouldBeEqual(statements.get(1), "/expected/weighted_average.sql");
        shouldBeEqual(statements.get(1), "/expected/weighted_average.sql");
        shouldBeEqual(statements.get(2), "/expected/payment.sql");
    }

    @Test
    public void shouldBatchPostgresFunctionsAndProcedures() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/stored-procedure-postgresql.sql"));
        m.migrate(null);
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(4);
        shouldBeEqual(statements.get(0), "/expected/getQtyOrders.sql");
        shouldBeEqual(statements.get(1), "/expected/one.sql");
        shouldBeEqual(statements.get(2), "/expected/emp_stamp.sql");
        a(statements.get(3).toString().trim()).shouldBeEqual("SELECT one();".trim());
    }

    @Test
    public void shouldUseTheSameDelimiterUntilExplicitlyChanged() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/function-mysql.sql"));
        m.migrate(null);
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(3);
        shouldBeEqual(statements.get(0), "/expected/drop_simple.sql");
        shouldBeEqual(statements.get(1), "/expected/simpleFunction.sql");
        shouldBeEqual(statements.get(2), "/expected/selectSimpleFunction.sql");
    }

    @Test
    public void shouldExecuteLastStatementWhenDelimiterIsMissing() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/missing-last-deliminator.sql"));
        m.migrate(null);
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(2);
        shouldBeEqual(statements.get(0), "/expected/create_users.sql");
        shouldBeEqual(statements.get(1), "/expected/roles.sql");
    }

    private void shouldBeEqual(Object operand1, String resourceName){
        a(stripNL(operand1)).shouldBeEqual(stripNL(readResource(resourceName)));
    }

    private String stripNL(Object in){
        return in.toString().trim().replace("\n", "").replace("\r", "");
    }
}
