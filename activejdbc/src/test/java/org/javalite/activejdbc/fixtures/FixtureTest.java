package org.javalite.activejdbc.fixtures;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Address;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.activejdbc.test_models.Room;
import org.junit.Test;

import static org.javalite.activejdbc.fixtures.AbstractFixtures.*;

/**
 * @author Evan Leonard
 *         Date: 7/21/12
 */
public class FixtureTest extends ActiveJDBCTest {

    @Test
    public void shouldCreatePerson() {
        given(Fixtures.class).person("Jim");

        final Person fixture = (Person) fixture("Jim");
        the(fixture).shouldNotBeNull();
        the(fixture.getString("name")).shouldEqual("Jim");
    }

    @Test
    public void shouldCreateUserWithAddress() {
        given(Fixtures.class).user("Bob").with().address("56 York St.");

        then("Bob").shouldHave(1, Address.class);
    }


    /**
     * This is an example of how you can manipulate fixtures after creating them
     * and then check for changes to the fixture
     */
    @Test
    public void shouldCreateUserAddThenAddAddress() {
        given(Fixtures.class).user("Bob");

        when("Bob").add(Address.createIt("address1","56 York St."));

        then("Bob").shouldHave(1, Address.class);
    }

    /**
     * This is an example of how you can quickly chain multiple levels of hierarchy
     */
    @Test
    public void shouldCreateUserWithAddressAndTwoRooms() {
        given(Fixtures.class).user("Joe").with().address("my house").with().room("living room").room("dining room");

        then("my house").shouldHave(2, Room.class);
        then("dining room").parent(Address.class).shouldBeFixture("my house");
    }

    /**
     * This illustrates an alternative syntax with 'in' to link models together
     */
    @Test
    public void shouldCreateTwoRoomsAtAndAddress() {
        given(Fixtures.class).room("living room").in().address("56 York St.")
                   .and().room("dining room").in().address("56 York St.");

        then("56 York St.").shouldHave(2, Room.class);
    }
}
