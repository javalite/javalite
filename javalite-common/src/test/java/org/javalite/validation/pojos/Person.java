package org.javalite.validation.pojos;

import org.javalite.validation.DateValidator;
import org.javalite.validation.EmailValidator;
import org.javalite.validation.ValidationSupport;

public class Person extends ValidationSupport {

    private String firstName, lastName, dob, email;

    public Person(String firstName, String lastName, String dob, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;

        validatePresenceOf("firstName", "lastName");
        validateWith(new DateValidator("dob", "yyyy-MM-dd"), new EmailValidator("email"));
    }
}
