package org.javalite.activejdbc.validation;

import org.junit.Test;

import static org.javalite.test.jspec.JSpec.the;

public class RegExpValidatorTest {

    @Test
    public void should(){
        Blog blog = new Blog("Helo");
        the(blog).shouldNotBe("valid");
        the(blog.errors().get("title")).shouldEqual("value does not match given format");

        blog.setTitle("Guns, Germs, and Steel");
        the(blog).shouldBe("valid");
    }
}
