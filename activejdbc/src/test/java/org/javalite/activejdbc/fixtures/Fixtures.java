package org.javalite.activejdbc.fixtures;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.test_models.*;

/**
 * This class is an example of how to write your own ActiveJDBC Fixtures
 *
 * Usage:
 * 1. Override Cleanup
 * 2. Author your Fixture methods in one of two styles:
 *      a) getFixture and addFixture methods
 *      b) FixtureFactory
 *
 * @author Evan Leonard
 *         Date: 7/21/12
 */
public class Fixtures extends AbstractFixtures<Fixtures> {

    /**
     * 1) Override cleanup_fixtures to delete any Models created by your Fixtures
     * This called when {@link AbstractFixtures#given(Class)} is called,
     * which initializes your fixtures.
     */
    @Override
    protected void cleanup_fixtures() {
        Account.deleteAll();
        User.deleteAll();
    }

    /**
     * 2a) Fixture written using getFixture and addFixture methods
     */
    public Fixtures person(String name) {
        Model role = getFixture(name);
        if(role == null) {
            role = Person.createIt("name", name,
                                 "last_name", "fake last name",
                                 "dob","1978-02-04",
                                 "graduation_date","2002-06-17");
        }
        addFixture(name, role);
        return this;
    }

    /**
     * 2b) Fixture written using FixtureFactory style
     */
    public Fixtures user(final String name) {
        return addFixture(name, new FixtureFactory() {
            public Model newFixture() {
                return User.createIt("first_name", name,
                        "last_name", "fake last name",
                        "email",  "fake@email.com");
            }
        });
    }

    public Fixtures address(final String address1) {
        return addFixture(address1, new FixtureFactory() {
            public Model newFixture() {
                return Address.createIt("address1", address1,
                        "address2", "second line",
                        "city", "Fixture City",
                        "state", "Colorado",
                        "zip", "88888");
            }
        });
    }

    public Fixtures room(final String name) {
        return addFixture(name, new FixtureFactory() {
            public Model newFixture() {
                return Room.createIt("name", name);
            }
        });
    }

}
