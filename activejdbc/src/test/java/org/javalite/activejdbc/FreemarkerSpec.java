package org.javalite.activejdbc;


import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.activejdbc.test_models.Student;
import org.javalite.common.Convert;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.javalite.activejdbc.test.JdbcProperties.driver;

/**
 * @author igor on 12/9/17.
 */
public class FreemarkerSpec extends ActiveJDBCTest {


    @Test
    public void shouldRenderSingleInstance() {
        deleteAndPopulateTable("people");

        Person smith = Person.findFirst("last_name = ?", "Smith");
        smith.set("graduation_date", null).saveIt();
        smith = Person.findFirst("last_name = ?", "Smith");
        the(smith.get("graduation_date")).shouldBeNull();
    }

    @Test
    public void shouldRenderList() {
        deleteAndPopulateTable("people");

        Person smith = Person.findFirst("last_name = ?", "Smith");
        smith.set("graduation_date", null).saveIt();

        List<Person> people = Person.findAll().orderBy("id");

        the(people.get(0).get("name")).shouldBeEqual("John");
        the(people.get(1).get("name")).shouldBeEqual("Leylah");
        the(people.get(2).get("name")).shouldBeEqual("Muhammad");
        the(people.get(3).get("name")).shouldBeEqual("Joe");
    }

    @Test
    public void shouldRenderRowProcessor() {
        deleteAndPopulateTable("students");

        Student cary = Student.findFirst("last_name = ?", "Cary");
        cary.set("enrollment_date", null).saveIt();

        List<Map> students = new ArrayList<>();
        Base.find("select * from students order by id").with(new RowListenerAdapter() {
            @Override
            public void onNext(Map<String, Object> row) {
                students.add(row);
            }
        });


        the(students.get(0).get("first_name")).shouldBeEqual("Jim");
        the(students.get(0).get("last_name")).shouldBeEqual("Cary");
        the(students.get(0).get("enrollment_date")).shouldBeNull();

        the(students.get(1).get("first_name")).shouldBeEqual("John");
        the(students.get(1).get("last_name")).shouldBeEqual("Carpenter");

        if(driver().equals("org.sqlite.JDBC")){
            the(students.get(1).get("enrollment_date")).shouldBeEqual("1987-01-29 13:00:00");
        } else if (driver().equals("com.mysql.cj.jdbc.Driver")) {
            java.sql.Timestamp ts = Timestamp.valueOf("1987-01-29 13:00:00");
            LocalDateTime ldt = Convert.toLocalDateTime(ts.getTime());
            the(students.get(1).get("enrollment_date")).shouldBeEqual(ldt);
        } else {
            java.sql.Timestamp ts = Timestamp.valueOf("1987-01-29 13:00:00");
            the(students.get(1).get("enrollment_date")).shouldBeEqual(ts);
        }
    }
}
