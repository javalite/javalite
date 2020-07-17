package org.javalite.validation;

import org.javalite.validation.pojos.Group;
import org.javalite.validation.pojos.Person;
import org.javalite.validation.pojos.Box;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.the;

public class ValidationSpec {

    @Test
    public void shouldValidatePresence() {
        Person person = new Person("John", "Doe", null, "johnb@doe.com");
        the(person).shouldBe("valid");

        person = new Person("John", null, null, null);
        the(person).shouldNotBe("valid");
        the(person.errors().size()).shouldBeEqual(2);
        the(person.errors().get("lastName")).shouldBeEqual("value is missing");
    }

    @Test
    public void shouldValidateDate() {
        Person person = new Person("John", "Doe", "2000-02-22", "johnb@doe.com");
        the(person).shouldBe("valid");
        person = new Person("John", "Doe", "abx-22-02", null);

        the(person).shouldNotBe("valid");
        the(person.errors().get("dob")).shouldBeEqual("attribute dob does not conform to format: yyyy-MM-dd");
    }

    @Test
    public void shouldValidateEmail() {
        Person person = new Person("John", "Doe", "2000-02-22", "john@doe.com");
        the(person).shouldBe("valid");

        person = new Person("John", "Doe", "2000-02-22", "john#doe.com");
        the(person).shouldNotBe("valid");
        the(person.errors().get("email")).shouldEqual("email has bad format");
    }

    @Test
    public void shouldValidateNumericality() {

        Group group = new Group("1");
        the(group).shouldBe("valid");

        group = new Group("blah");
        the(group).shouldNotBe("valid");
        the(group.errors().get("size")).shouldEqual("value is not a number");
    }

    @Test
    public void shouldValidateRange(){
        Box box = new Box(3);
        the(box).shouldBe("valid");

        box = new Box(30);
        the(box).shouldNotBe("valid");
        the(box.errors().get("width")).shouldEqual("value should be within limits: > 1 and < 10");
    }

}
