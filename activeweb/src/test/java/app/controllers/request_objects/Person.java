package app.controllers.request_objects;

public class Person {
    private String  firstName, lastName;
    private int yearOfBirth = -1;
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
