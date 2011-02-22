package activejdbc.examples.simple;

import activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleExample {
    final static Logger logger = LoggerFactory.getLogger(SimpleExample.class);
    public static void main(String[] args) {
        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/activejdbc", "root", "p@ssw0rd");

        createEmployee();
        logger.info("=========> Created employee:");
        selectEmployee();
        updateEmployee();
        logger.info("=========> Updated employee:");
        selectAllEmployees();
        deleteEmployee();
        logger.info("=========> Deleted employee:");
        selectAllEmployees();
        createEmployee();
        logger.info("=========> Created employee:");
        selectEmployee();
        deleteAllEmployees();
        logger.info("=========> Deleted all employees:");
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
        Employee e = (Employee)Employee.findFirst("first_name = ?", "John");
        logger.info(e.toString());
    }

    private static void updateEmployee() {
        Employee e = (Employee)Employee.findFirst("first_name = ?", "John");
        e.set("last_name", "Steinbeck").saveIt();
    }

    private static void deleteEmployee() {
        Employee e = (Employee)Employee.findFirst("first_name = ?", "John");
        e.delete();
    }

    private static void deleteAllEmployees() {
            Employee.deleteAll();
    }

    private static void selectAllEmployees() {
            logger.info("Employees list: " + Employee.findAll());
    }
}
