package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.User;
import org.javalite.activejdbc.validation.length.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

public class AttributeLengthValidatorTest extends ActiveJDBCTest {

    @Before
    public void setUp() {
        deleteAndPopulateTables("users");
    }

    @Test
    public void testMinLength() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Min.of(4));

        User u = new User();
        u.set("email", "john@doe.com");
        u.set("first_name", "Igor");

        User.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(0);
        User.removeValidator(validator);

        validator = AttributeLengthValidator.on("first_name").with(Min.of(5));
        User.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
        a(u.errors().get("first_name")).shouldBeEqual("attribute should have a minimum length of 5");
        User.removeValidator(validator);
    }

    @Test
    public void testMaxLength() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Max.of(10));

        User u = new User();
        u.set("email", "john@doe.com");
        u.set("first_name", "Igor");

        User.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(0);

        User.removeValidator(validator);
        validator = AttributeLengthValidator.on("first_name").with(Max.of(3));
        User.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
        a(u.errors().get("first_name")).shouldBeEqual("attribute should have a maximum length of 3");
        User.removeValidator(validator);
    }

    @Test
    public void testExactLength() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Exact.of(4));

        User u = new User();
        u.set("email", "john@doe.com");
        u.set("first_name", "Igor");

        User.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(0);

        User.removeValidator(validator);
        validator = AttributeLengthValidator.on("first_name").with(Exact.of(3));
        User.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
        a(u.errors().get("first_name")).shouldBeEqual("attribute should have an exact length of 3");
        User.removeValidator(validator);
    }

    @Test
    public void testRangeLength() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Range.of(0, 5));

        User u = new User();
        u.set("email", "john@doe.com");
        u.set("first_name", "Igor");

        User.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(0);

        User.removeValidator(validator);
        validator = AttributeLengthValidator.on("first_name").with(Range.of(5, 5));
        User.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
        a(u.errors().get("first_name")).shouldBeEqual("attribute should have a length between 5 and 5 (inclusive)");
        User.removeValidator(validator);
    }

    @Test
    public void testNonStringFieldValueThrowsException() {
        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Min.of(2));

        User u = new User();
        u.set("email", "john@doe.com");
        u.set("first_name", 5);
        User.addValidator(validator);
        try {
            u.validate();
            a(false);
        } catch (final IllegalArgumentException e) {
            a(true);
            a(e.getMessage().equals("Attribute must be a String"));
        }finally {
            User.removeValidator(validator);
        }
    }

    @Test
    public void testErrorsWithInternationalization() {

        User u = new User();
        u.set("email", "john@doe.com");
        u.set("first_name", "Igor");

        AttributeLengthValidator validator = AttributeLengthValidator.on("first_name").with(Range.of(5, 5));
        validator.setMessage("validation.length.range");
        User.addValidator(validator);
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
        a(u.errors().get("first_name")).shouldBeEqual("Attribute should have a length between 5 and 5 (inclusive).");
        a(u.errors(new Locale("de", "DE")).get("first_name")).shouldBeEqual("Attribut sollte eine L\u00E4nge zwischen 5 und 5 (inklusive).");
        User.removeValidator(validator);
    }
}
