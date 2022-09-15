package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.SalariedEmployee;
import org.junit.Before;
import org.junit.Test;

import java.sql.Array;
import java.sql.SQLException;
import java.util.List;

import static org.javalite.activejdbc.test.JdbcProperties.url;

/**
 * related to:
 * https://github.com/javalite/javalite/issues/1214
 */


public class ArrayTypeSpec extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        if (notPostgreSQL()) return;

        deleteAndPopulateTable("salaried_employees");
    }

    @Test
    public void shouldReadArray() throws SQLException {
        if (notPostgreSQL()) return;

        List<SalariedEmployee> list = SalariedEmployee.findAll().orderBy("id");

        the(list.size()).shouldEqual(2);

        //first row
        Array array = list.get(0).getArray("schedule");
        String[][] schedules = (String[][]) array.getArray();
        the(schedules[0][0]).shouldBeEqual("meeting");
        the(schedules[0][1]).shouldBeEqual("lunch");
        the(schedules[1][0]).shouldBeEqual("training");
        the(schedules[1][1]).shouldBeEqual("presentation");

        array = list.get(0).getArray("pay_by_quarter");
        Integer[] pay = (Integer[]) array.getArray();
        the(pay[0]).shouldBeEqual(10001);
        the(pay[1]).shouldBeEqual(10002);
        the(pay[2]).shouldBeEqual(10003);
        the(pay[3]).shouldBeEqual(10004);


        //second row
        array = list.get(1).getArray("schedule");
        schedules = (String[][]) array.getArray();
        the(schedules[0][0]).shouldBeEqual("breakfast");
        the(schedules[0][1]).shouldBeEqual("consulting");
        the(schedules[1][0]).shouldBeEqual("meeting");
        the(schedules[1][1]).shouldBeEqual("lunch");

        array = list.get(1).getArray("pay_by_quarter");
        pay = (Integer[]) array.getArray();
        the(pay[0]).shouldBeEqual(20000);
        the(pay[1]).shouldBeEqual(25000);
        the(pay[2]).shouldBeEqual(25001);
        the(pay[3]).shouldBeEqual(25002);
    }

    @Test
    public void shouldSaveArrays() throws SQLException {

        if (notPostgreSQL()) return;

        SalariedEmployee salariedEmployee = new SalariedEmployee();


        salariedEmployee
                .setArray("schedule", new String[][]{{"getup", "lay down"}, {"stand", "run"}})
                .setArray("pay_by_quarter", new Integer[]{1, 2, 3, 4});



        salariedEmployee.saveIt();


        List<SalariedEmployee> list = SalariedEmployee.findAll().orderBy("id");
        the(list.size()).shouldEqual(3);
        Integer[] payIntegers = (Integer[]) list.get(2).getArray("pay_by_quarter").getArray();

        the(payIntegers[0]).shouldBeEqual(1);
        the(payIntegers[1]).shouldBeEqual(2);
        the(payIntegers[2]).shouldBeEqual(3);
        the(payIntegers[3]).shouldBeEqual(4);

        String[][] schedules = (String[][]) list.get(2).getArray("schedule").getArray();

        the(schedules[0][0]).shouldBeEqual("getup");
        the(schedules[0][1]).shouldBeEqual("lay down");
        the(schedules[1][0]).shouldBeEqual("stand");
        the(schedules[1][1]).shouldBeEqual("run");
    }

    private boolean notPostgreSQL() {
        return !url().contains("postgre");
    }
}
