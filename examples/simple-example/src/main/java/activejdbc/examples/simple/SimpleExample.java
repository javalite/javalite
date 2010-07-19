package activejdbc.examples.simple;

import activejdbc.Base;

public class SimpleExample {
    public static void main(String[] args) {
        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test", "root", "p@ssw0rd");

        createEmployee();
        System.out.println("=========> Created employee:");
        selectEmployee();
        updateEmployee();
        System.out.println("=========> Updated employee:");
        selectAllEmployees();
        deleteEmployee();
        System.out.println("=========> Deleted employee:");
        selectAllEmployees();
        createEmployee();
        System.out.println("=========> Created employee:");
        selectEmployee();
        deleteAllEmployees();
        System.out.println("=========> Deleted all employees:");
        selectAllEmployees();

        Base.close();
    }

    private static void createEmployee() {
        Employee e = new Employee();
        e.set("first_name", "John");
        e.set("last_name", "Doe");
        e.saveIt();
    }

    private static void selectEmployee() {
        Employee e = Employee.findFirst("first_name = ?", "John");
        System.out.println(e);
    }

    private static void updateEmployee() {
        Employee e = Employee.findFirst("first_name = ?", "John");
        e.set("last_name", "Steinbeck").saveIt();
    }

    private static void deleteEmployee() {
        Employee e = Employee.findFirst("first_name = ?", "John");
        e.delete();
    }

    private static void deleteAllEmployees() {
            Employee.deleteAll();
    }

    private static void selectAllEmployees() {
            System.out.println("Employees list: " + Employee.findAll());
    }
}
