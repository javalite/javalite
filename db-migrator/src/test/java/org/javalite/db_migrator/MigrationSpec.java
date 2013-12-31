package org.javalite.db_migrator;


import org.javalite.activejdbc.Base;
import org.javalite.db_migrator.mock.MockConnection;
import org.javalite.db_migrator.mock.MockDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.javalite.test.jspec.JSpec.a;

public class MigrationSpec {

    @Before
    public void before() {
        Base.open(new MockDataSource());  //new mock connection every time
    }

    @After
    public void after() {
        Base.close();
    }

    @Test
    public void shouldBatchSimpleCommands() throws Exception {

        Migration m = new Migration("123", new File("src/test/resources/sql/simple.sql"));

        m.migrate();
        List statements = getStatements();

        a(statements.size()).shouldBeEqual(2);
        a(statements.get(0)).shouldBeEqual("create table users ( username varchar not null, password varchar not null )");
        a(statements.get(1)).shouldBeEqual("alter table users add index (username), add unique (username)");
    }

    private List getStatements() {
        return ((MockConnection) Base.connection()).getExecutedStatements();
    }


    @Test
    public void shouldHandleComplexCommands() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/complex.sql"));
        m.migrate();
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(1);
        a(statements.get(0)).shouldBeEqual("update dav_file set parent = ( select id from ( select id from dav_file where name = '__SITE_PROTECTED__' ) as x ) where ( name = 'templates' and parent is null ) or ( name = 'velocity' and parent is null ) or ( name = 'tags' and parent is null ) or ( name = 'ctd' and parent is null )");
    }

    @Test
    public void shouldBatchMySQLFunctionsAndProcedures() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/stored-procedure-mysql.sql"));
        m.migrate();
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(3);
        a(statements.get(0)).shouldBeEqual("CREATE FUNCTION hello (s CHAR(20)) RETURNS CHAR(50) DETERMINISTIC RETURN CONCAT('Hello, ',s,'!')");
        a(statements.get(1)).shouldBeEqual("CREATE FUNCTION weighted_average (n1 INT, n2 INT, n3 INT, n4 INT) RETURNS INT DETERMINISTIC BEGIN DECLARE avg INT; SET avg = (n1+n2+n3*2+n4*4)/8; RETURN avg; END");
        a(statements.get(2)).shouldBeEqual("CREATE PROCEDURE payment(payment_amount DECIMAL(6,2), payment_seller_id INT) BEGIN DECLARE n DECIMAL(6,2); SET n = payment_amount - 1.00; INSERT INTO Moneys VALUES (n, CURRENT_DATE); IF payment_amount > 1.00 THEN UPDATE Sellers SET commission = commission + 1.00 WHERE seller_id = payment_seller_id; END IF; END");
    }

    @Test
    public void shouldBatchPostgresFunctionsAndProcedures() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/stored-procedure-postgresql.sql"));
        m.migrate();
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(4);
        a(statements.get(0)).shouldBeEqual("CREATE FUNCTION getQtyOrders(customerID int) RETURNS int AS $$ DECLARE qty int; BEGIN SELECT COUNT(*) INTO qty FROM Orders WHERE accnum = customerID; RETURN qty; END; $$ LANGUAGE plpgsql");
        a(statements.get(1)).shouldBeEqual("CREATE FUNCTION one() RETURNS integer AS ' SELECT 1 AS result; ' LANGUAGE SQL");
        a(statements.get(2)).shouldBeEqual("CREATE FUNCTION emp_stamp() RETURNS trigger AS $emp_stamp$ BEGIN IF NEW.empname IS NULL THEN RAISE EXCEPTION 'empname cannot be null'; END IF; IF NEW.salary IS NULL THEN RAISE EXCEPTION '% cannot have null salary', NEW.empname; END IF; IF NEW.salary < 0 THEN RAISE EXCEPTION '% cannot have a negative salary', NEW.empname; END IF; NEW.last_date := current_timestamp; NEW.last_user := current_user; RETURN NEW; END; $emp_stamp$ LANGUAGE plpgsql");
        a(statements.get(3)).shouldBeEqual("SELECT one()");
    }

    @Test
    public void shouldUseTheSameDelimiterUntilExplicitlyChanged() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/function-mysql.sql"));
        m.migrate();
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(3);
        a(statements.get(0)).shouldBeEqual("DROP FUNCTION IF EXISTS simpleFunction");
        a(statements.get(1)).shouldBeEqual("CREATE FUNCTION simpleFunction() RETURNS varchar(100) READS SQL DATA begin declare message varchar(100) default 'Hello Word'; return message; end");
        a(statements.get(2)).shouldBeEqual("select simpleFunction()");
    }

    @Test
    public void shouldExecuteLastStatementWhenDelimiterIsMissing() throws Exception {
        Migration m = new Migration("123", new File("src/test/resources/sql/missing-last-deliminator.sql"));
        m.migrate();
        List statements = getStatements();
        a(statements.size()).shouldBeEqual(2);
        a(statements.get(0)).shouldBeEqual("create table users ( username varchar not null, password varchar not null )");
        a(statements.get(1)).shouldBeEqual("create table roles ( name varchar not null unique, description text not null )");
    }
}
