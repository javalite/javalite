package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.User;
import org.javalite.activejdbc.validation.length.*;
import org.javalite.activejdbc.validation.length.option.Exact;
import org.javalite.activejdbc.validation.length.option.Max;
import org.javalite.activejdbc.validation.length.option.Min;
import org.javalite.activejdbc.validation.length.option.Range;
import org.junit.Before;
import org.junit.Test;

public class AttributeLengthValidatorTest extends ActiveJDBCTest {

    @Before
    public void setUp() {
        deleteAndPopulateTables("users");
    }

    @Test
    public void testMinLength() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Min.of(4));

        User u = new User();
        u.set("email", "igor@polevoy.org");
        u.set("first_name", "Igor");

        u.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(0);
        u.removeValidator(validator);

        validator = AttributeLengthValidator.on("first_name").with(Min.of(5));
        u.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
        u.removeValidator(validator);
    }

    @Test
    public void testMaxLength() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Max.of(10));

        User u = new User();
        u.set("email", "igor@polevoy.org");
        u.set("first_name", "Igor");

        u.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(0);

        u.removeValidator(validator);
        validator = AttributeLengthValidator.on("first_name").with(Max.of(3));
        u.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
        u.removeValidator(validator);
    }

    @Test
    public void testExactLength() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Exact.of(4));

        User u = new User();
        u.set("email", "igor@polevoy.org");
        u.set("first_name", "Igor");

        u.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(0);

        u.removeValidator(validator);
        validator = AttributeLengthValidator.on("first_name").with(Exact.of(3));
        u.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
        u.removeValidator(validator);
    }

    @Test
    public void testRangeLength() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Range.of(0, 5));

        User u = new User();
        u.set("email", "igor@polevoy.org");
        u.set("first_name", "Igor");

        u.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(0);

        u.removeValidator(validator);
        validator = AttributeLengthValidator.on("first_name").with(Range.of(5, 5));
        u.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
        u.removeValidator(validator);
    }

    @Test
    public void testNonStringFieldValueThrowsException() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Min.of(2));

        User u = new User();
        u.set("email", "igor@polevoy.org");
        u.set("first_name", 5);
        u.addValidator(validator);
        try {
            u.validate();
            a(false);
        } catch (final IllegalArgumentException e) {
            a(true);
            a(e.getMessage().equals("Attribute must be a String"));
        }
    }
}
