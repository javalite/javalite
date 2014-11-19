/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package org.javalite.activejdbc;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Student;
import org.junit.Test;

public class ConverterTest extends ActiveJDBCTest {

    @Test
    public void shouldSetAndGetDateAsString() {
        Student student = new Student().setDate("dob", "11/15/2007");

        the(student.get("dob")).shouldBeA(java.sql.Date.class);
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime((java.util.Date) student.get("dob"));
        the(cal.get(Calendar.DAY_OF_MONTH)).shouldBeEqual(15);
        the(cal.get(Calendar.MONTH)).shouldBeEqual(11 - 1);
        the(cal.get(Calendar.YEAR)).shouldBeEqual(2007);

        the(student.getString("dob")).shouldBeEqual("11/15/2007");
    }

    @Test
    public void shouldSetNullDate() {
        Student student = new Student().setDate("dob", null);
        the(student.get("dob")).shouldBeNull();
    }

    @Test
    public void shouldSetAndGetStringAsDate() {
        java.sql.Date date = getDate(2007, 11, 15);
        Student student = new Student().setString("dob", date);

        the(student.get("dob")).shouldBeA(String.class);
        the(student.get("dob")).shouldBeEqual("11/15/2007");

        the(student.getDate("dob").getTime()).shouldBeEqual(date.getTime());
    }

    @Test
    public void shouldSetNullString() {
        Student student = new Student().setString("dob", null);
        the(student.get("dob")).shouldBeNull();
    }

    @Test
    public void shouldGetConvertDateToString() {
        deleteAndPopulateTable("students");
        the(Student.findById(1).getString("dob")).shouldBeEqual("12/01/1965");
        the(Student.findById(2).getString("dob")).shouldBeEqual("12/01/1979");
    }

    @Test
    public void shouldSetAndGetTimestampAsString() {
        Student student = new Student().setTimestamp("enrollment_date", "11/16/2014 10 PM");

        the(student.get("enrollment_date")).shouldBeA(java.sql.Timestamp.class);
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime((java.util.Date) student.get("enrollment_date"));
        the(cal.get(Calendar.DAY_OF_MONTH)).shouldBeEqual(16);
        the(cal.get(Calendar.MONTH)).shouldBeEqual(11 - 1);
        the(cal.get(Calendar.YEAR)).shouldBeEqual(2014);
        the(cal.get(Calendar.HOUR_OF_DAY)).shouldBeEqual(22);
        the(cal.get(Calendar.MINUTE)).shouldBeEqual(0);
        the(cal.get(Calendar.SECOND)).shouldBeEqual(0);
        the(cal.get(Calendar.MILLISECOND)).shouldBeEqual(0);

        the(student.getString("enrollment_date")).shouldBeEqual("11/16/2014 10 PM");
    }

    @Test
    public void shouldSetNullTimestamp() {
        Student student = new Student().setTimestamp("enrollment_date", null);
        the(student.get("enrollment_date")).shouldBeNull();
    }

    @Test
    public void shouldSetAndGetStringAsTimestamp() {
        java.sql.Timestamp timestamp = getTimestamp(2014, 11, 16, 22, 0, 0, 0);
        Student student = new Student().setString("enrollment_date", timestamp);

        the(student.get("enrollment_date")).shouldBeA(String.class);
        the(student.get("enrollment_date")).shouldBeEqual("11/16/2014 10 PM");

        the(student.getTimestamp("enrollment_date").getTime()).shouldBeEqual(timestamp.getTime());
    }

    @Test
    public void shouldGetConvertTimestampToString() {
        deleteAndPopulateTable("students");
        the(Student.findById(1).getString("enrollment_date")).shouldBeEqual("01/20/1973 11 AM");
        the(Student.findById(2).getString("enrollment_date")).shouldBeEqual("01/29/1987 1 PM");
    }

    @Test
    public void shouldSaveAndFetchDateAndTimestamp() {
        Student student = Student.create("first_name", "Joe", "last_name", "Shmoe")
                .setDate("dob", "02/29/2000")
                .setTimestamp("enrollment_date", "02/29/2008 12 PM");
        student.saveIt();
        Long id = student.getLongId();

        student = Student.findById(id);
        the(student.get("dob")).shouldBeA(java.sql.Date.class);
        the(student.getString("dob")).shouldBeEqual("02/29/2000");
        the(student.get("enrollment_date")).shouldBeA(java.sql.Timestamp.class);
        the(student.getString("enrollment_date")).shouldBeEqual("02/29/2008 12 PM");
    }

    @Test
    public void shouldCreateBlankStringAsNull() {
        Student student = Student.create("first_name", "", "last_name", " ");
        the(student.get("first_name")).shouldBeNull();
        the(student.get("last_name")).shouldBeNull();
    }

    @Test
    public void shouldSetBlankStringAsNull() {
        Student student = new Student().set("first_name", "").set("last_name", "\t ");
        the(student.get("first_name")).shouldBeNull();
        the(student.get("last_name")).shouldBeNull();

        student = new Student().setString("first_name", "    \t").setString("last_name", "");
        the(student.get("first_name")).shouldBeNull();
        the(student.get("last_name")).shouldBeNull();
    }

    @Test
    public void shouldSetNull() {
        Student student = new Student().set("first_name", null).setString("last_name", null);
        the(student.get("first_name")).shouldBeNull();
        the(student.get("last_name")).shouldBeNull();
    }
}
