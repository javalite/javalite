package app.controllers.request_objects;

import org.javalite.validation.ValidationSupport;

public class Person2 extends ValidationSupport {
    private String  firstName, lastName;
    private Integer yearOfBirth = -1;
    private boolean married = false;



    /**
     * Only used in tests. Do not need in a real app.
     */
    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", yearOfBirth=" + yearOfBirth +
                ", married=" + married +
                '}';
    }
}
