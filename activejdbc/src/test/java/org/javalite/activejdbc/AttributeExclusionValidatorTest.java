package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Student;
import org.javalite.activejdbc.validation.exclusion.AttributeExclusionValidator;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

public class AttributeExclusionValidatorTest extends ActiveJDBCTest {

    @Test
    public void testWhenAttributeIsExcluded() {
        AttributeExclusionValidator validator = AttributeExclusionValidator.on("first_name").with("Larry", "John");

        Student s = new Student();
        s.set("first_name", "John");
        s.set("last_name", "Doe");

        Student.addValidator(validator);
        s.validate();
        a(s.errors().size()).shouldBeEqual(1);
        Student.removeValidator(validator);
    }

    @Test
    public void testWhenAttributeIsNotExcluded() {
        AttributeExclusionValidator validator = AttributeExclusionValidator.on("dob").with(new Date(500), new Date(800));

        Student s = new Student();
        s.set("first_name", "John");
        s.set("last_name", "Doe");
        s.set("dob", new Date(550));

        Student.addValidator(validator);
        s.validate();
        a(s.errors().size()).shouldBeEqual(0);
        Student.removeValidator(validator);
    }

    @Test
    public void testWithInternationalization() {
        AttributeExclusionValidator validator = AttributeExclusionValidator.on("dob").with(new Date(500), new Date(800));
        validator.setMessage("validation.exclusion");

        Student s = new Student();
        s.set("first_name", "John");
        s.set("last_name", "Doe");
        s.set("dob", new Date(800));

        Student.addValidator(validator);
        s.validate();
        a(s.errors().size()).shouldBeEqual(1);
        a(s.errors().get("dob")).shouldBeEqual("dob is reserved.");
        a(s.errors(new Locale("de", "DE")).get("dob")).shouldBeEqual("dob ist reserviert.");
        Student.removeValidator(validator);
    }
}
