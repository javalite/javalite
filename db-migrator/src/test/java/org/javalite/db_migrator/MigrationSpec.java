package org.javalite.db_migrator;

import org.javalite.db_migrator.mock.MockConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;
import static org.javalite.db_migrator.DbUtils.*;

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

        assertEquals(statements.size(), 2);
        assertEquals(statements.get(0), "create table users ( username varchar not null, password varchar not null )");
        assertEquals(statements.get(1), "alter table users add index (username), add unique (username)");
    }

    private List getStatements() {
        return connection.getExecutedStatements();
    }

    @Test
    public void shouldHandleComplexCommands() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/complex.sql"));
        m.migrate(null);
        List statements = getStatements();
        assertEquals(statements.size(), 1);
        assertEquals(statements.get(0), "update dav_file set parent = ( select id from ( select id from dav_file where name = '__SITE_PROTECTED__' ) as x ) where ( name = 'templates' and parent is null ) or ( name = 'velocity' and parent is null ) or ( name = 'tags' and parent is null ) or ( name = 'ctd' and parent is null )");
    }

    @Test
    public void shouldBatchMySQLFunctionsAndProcedures() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/stored-procedure-mysql.sql"));
        m.migrate(null);
        List statements = getStatements();
        assertEquals(statements.size(), 3);
        assertEquals(statements.get(0), "CREATE FUNCTION hello (s CHAR(20)) RETURNS CHAR(50) DETERMINISTIC RETURN CONCAT('Hello, ',s,'!')");
        assertEquals(statements.get(1), "CREATE FUNCTION weighted_average (n1 INT, n2 INT, n3 INT, n4 INT) RETURNS INT DETERMINISTIC BEGIN DECLARE avg INT; SET avg = (n1+n2+n3*2+n4*4)/8; RETURN avg; END");
        assertEquals(statements.get(2), "CREATE PROCEDURE payment(payment_amount DECIMAL(6,2), payment_seller_id INT) BEGIN DECLARE n DECIMAL(6,2); SET n = payment_amount - 1.00; INSERT INTO Moneys VALUES (n, CURRENT_DATE); IF payment_amount > 1.00 THEN UPDATE Sellers SET commission = commission + 1.00 WHERE seller_id = payment_seller_id; END IF; END");
    }

    @Test
    public void shouldBatchPostgresFunctionsAndProcedures() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/stored-procedure-postgresql.sql"));
        m.migrate(null);
        List statements = getStatements();
        assertEquals(statements.size(), 4);
        assertEquals(statements.get(0), "CREATE FUNCTION getQtyOrders(customerID int) RETURNS int AS $$ DECLARE qty int; BEGIN SELECT COUNT(*) INTO qty FROM Orders WHERE accnum = customerID; RETURN qty; END; $$ LANGUAGE plpgsql");
        assertEquals(statements.get(1), "CREATE FUNCTION one() RETURNS integer AS ' SELECT 1 AS result; ' LANGUAGE SQL");
        assertEquals(statements.get(2), "CREATE FUNCTION emp_stamp() RETURNS trigger AS $emp_stamp$ BEGIN IF NEW.empname IS NULL THEN RAISE EXCEPTION 'empname cannot be null'; END IF; IF NEW.salary IS NULL THEN RAISE EXCEPTION '% cannot have null salary', NEW.empname; END IF; IF NEW.salary < 0 THEN RAISE EXCEPTION '% cannot have a negative salary', NEW.empname; END IF; NEW.last_date := current_timestamp; NEW.last_user := current_user; RETURN NEW; END; $emp_stamp$ LANGUAGE plpgsql");
        assertEquals(statements.get(3), "SELECT one()");
    }

    @Test
    public void shouldUseTheSameDelimiterUntilExplicitlyChanged() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/function-mysql.sql"));
        m.migrate(null);
        List statements = getStatements();
        assertEquals(statements.size(), 3);
        assertEquals(statements.get(0), "DROP FUNCTION IF EXISTS simpleFunction");
        assertEquals(statements.get(1), "CREATE FUNCTION simpleFunction() RETURNS varchar(100) READS SQL DATA begin declare message varchar(100) default 'Hello Word'; return message; end");
        assertEquals(statements.get(2), "select simpleFunction()");
    }

    @Test
    public void shouldExecuteLastStatementWhenDelimiterIsMissing() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/missing-last-deliminator.sql"));
        m.migrate(null);
        List statements = getStatements();
        assertEquals(statements.size(), 2);
        assertEquals(statements.get(0), "create table users ( username varchar not null, password varchar not null )");
        assertEquals(statements.get(1), "create table roles ( name varchar not null unique, description text not null )");
    }

    @Test
    public void shouldHandleNewLinesAndSpacesInStatements() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/newlines_and_spaces.sql"));
        m.migrate(null);
        List statements = getStatements();

        for (Object o : statements) {

            String statement = o.toString().trim();
            if(!statement.equals("")){

                System.out.println("===");
                System.out.println(o);
            }

        }
    }
}
