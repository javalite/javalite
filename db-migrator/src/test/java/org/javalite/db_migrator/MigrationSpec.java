package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.javalite.common.Util;
import org.javalite.db_migrator.mock.MockConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.javalite.common.Util.getResourceLines;
import static org.javalite.test.jspec.JSpec.a;


public class MigrationSpec {

    MockConnection connection;

    @Before
    public void before() {
        connection = new MockConnection();
        Base.attach(connection);
    }

    @After
    public void after(){
        Base.detach();
    }



    @Test
    public void shouldBatchSimpleCommands() {

        File f = new File("src/test/resources/sql/simple.sql");
        SQLMigration m = new SQLMigration("123", f.getName(), Util.readFile(f), null);

        m.migrate();
        List statements = getStatements();

        a(statements.size()).shouldBeEqual(2);
        shouldBeEqual(statements.get(0), "/expected/create_users.sql");
        shouldBeEqual(statements.get(1), "/expected/alter_users.sql");
    }

    private List getStatements() {
        return connection.getExecutedStatements();
    }

    @Test
    public void shouldHandleComplexCommands()  {
        File f = new File("src/test/resources/sql/complex.sql");
        SQLMigration m = new SQLMigration("123", f.getName(), Util.readFile(f) , null);
        m.migrate();
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(1);
        shouldBeEqual(statements.get(0), "/expected/dav_file.sql");
    }

    @Test
    public void shouldBatchMySQLFunctionsAndProcedures() {
        File f = new File("src/test/resources/sql/stored-procedure-mysql.sql");
        SQLMigration m = new SQLMigration("123", f.getName(), Util.readFile(f) , null);
        m.migrate();
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(3);
        a(statements.get(0)).shouldBeEqual("CREATE FUNCTION hello (s CHAR(20)) RETURNS CHAR(50) DETERMINISTIC RETURN CONCAT('Hello, ',s,'!')");
        shouldBeEqual(statements.get(1), "/expected/weighted_average.sql");
        shouldBeEqual(statements.get(1), "/expected/weighted_average.sql");
        shouldBeEqual(statements.get(2), "/expected/payment.sql");
    }

    @Test
    public void shouldBatchPostgresFunctionsAndProcedures(){
        File f = new File("src/test/resources/sql/stored-procedure-postgresql.sql");
        SQLMigration m = new SQLMigration("123", f.getName(), Util.readFile(f), null);
        m.migrate();
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(4);
        shouldBeEqual(statements.get(0), "/expected/getQtyOrders.sql");
        shouldBeEqual(statements.get(1), "/expected/one.sql");
        shouldBeEqual(statements.get(2), "/expected/emp_stamp.sql");
        a(statements.get(3).toString().trim()).shouldBeEqual("SELECT one();".trim());
    }

    @Test
    public void shouldUseTheSameDelimiterUntilExplicitlyChanged() {
        File f =  new File("src/test/resources/sql/function-mysql.sql");
        SQLMigration m = new SQLMigration("123", f.getName(), Util.readFile(f), null);
        m.migrate();
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(3);
        shouldBeEqual(statements.get(0), "/expected/drop_simple.sql");
        shouldBeEqual(statements.get(1), "/expected/simpleFunction.sql");
        shouldBeEqual(statements.get(2), "/expected/selectSimpleFunction.sql");
    }

    @Test
    public void shouldExecuteLastStatementWhenDelimiterIsMissing() {
        File f = new File("src/test/resources/sql/missing-last-deliminator.sql");
        SQLMigration m = new SQLMigration("123", f.getName(), Util.readFile(f), null);
        m.migrate();
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

    private String readResource(String resourceName) {
        try {
            List<String> lines = getResourceLines(resourceName);
            StringBuffer buffer = new StringBuffer();
            lines.forEach(s -> buffer.append(s.trim()).append('\n'));
            return buffer.toString();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
