package activejdbc;

import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.Person;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class InListQueryTest extends ActiveJDBCTest {


    @Test
    public void shouldSelectCorrectPeople(){
        resetTable("people");

        String names[] ={"John", "Leylah"};
        a(Person.where("name in (?)", names).size()).shouldBeEqual(2);
    }
}
