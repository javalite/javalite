package activejdbc.examples.multidb;

import activejdbc.DB;

public class Main {
    public static void main(String[] args) {
        new DB("corporation").open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test", "root", "p@ssw0rd");
        new DB("university").open("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@localhost:1521:xe", "activejdbc", "activejdbc");

        Employee.deleteAll();
        Student.deleteAll();

        Employee.createIt("first_name", "John", "last_name", "Doe");
        Employee.createIt("first_name", "Jane", "last_name", "Smith");

        Student.createIt("first_name", "Mike", "last_name", "Myers");
        Student.createIt("first_name", "Steven", "last_name", "Spielberg");

        System.out.println("*** Employees ***");
        Employee.findAll().dump();
        System.out.println("*** Students ***");
        Student.findAll().dump();

        new DB("corporation").close();
        new DB("university").close();
    }
}
