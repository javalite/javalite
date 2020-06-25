package app.controllers.request_objects;

public class Person {
    public String  firstName, lastName;
    public int yearOfBirth = -1;
    public boolean married = false;


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
