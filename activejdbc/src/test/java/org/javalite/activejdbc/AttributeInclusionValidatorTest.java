package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Student;
import org.javalite.activejdbc.validation.inclusion.AttributeInclusionValidator;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

public class AttributeInclusionValidatorTest extends ActiveJDBCTest {

    @Test
    public void testWhenAttributeIsIncluded() {
        AttributeInclusionValidator validator = AttributeInclusionValidator.on("first_name").with("Larry", "John");

        Student s = new Student();
        s.set("first_name", "John");
        s.set("last_name", "Doe");

        Student.addValidator(validator);
        s.validate();
        a(s.errors().size()).shouldBeEqual(0);
        Student.removeValidator(validator);
    }

    @Test
    public void testWhenAttributeIsNotIncluded() {
        AttributeInclusionValidator validator = AttributeInclusionValidator.on("dob").with(new Date(500), new Date(800));

        Student s = new Student();
        s.set("first_name", "John");
        s.set("last_name", "Doe");
        s.set("dob", new Date(550));

        Student.addValidator(validator);
        s.validate();
        a(s.errors().size()).shouldBeEqual(1);
        Student.removeValidator(validator);
    }

    @Test
    public void testWithInternationalization() {
        AttributeInclusionValidator validator = AttributeInclusionValidator.on("dob").with(new Date(500), new Date(800));
        validator.setMessage("validation.inclusion");

        Student s = new Student();
        s.set("first_name", "John");
        s.set("last_name", "Doe");
        s.set("dob", new Date(550));

        Student.addValidator(validator);
        s.validate();
        a(s.errors().size()).shouldBeEqual(1);
        a(s.errors().get("dob")).shouldBeEqual("dob is not included in the list.");
        a(s.errors(new Locale("de", "DE")).get("dob")).shouldBeEqual("dob ist nicht in der Liste enthalten.");
        Student.removeValidator(validator);
    }
}
