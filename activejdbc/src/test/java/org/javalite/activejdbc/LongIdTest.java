package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Alarm;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class LongIdTest extends ActiveJDBCTest {

    @Test
    public void shouldConvertIdToLong() {
        Alarm alarm = new Alarm();
        the(alarm.getLongId()).shouldBeNull();
    }

    @Test
    public void shouldReturnValidLongId(){
        deleteAndPopulateTable("people");
        Person p = Person.findById(1);
        a(p.getLongId()).shouldBeEqual(1);
    }
}
