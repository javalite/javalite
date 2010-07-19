package activejdbc.examples.simple;

import activejdbc.Base;

import java.sql.SQLException;

/**
 * @author Igor Polevoy
 */
public class InsertAll {
    public static void main(String[] args) throws SQLException {
        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test", "root", "p@ssw0rd");
        Base.connection().setAutoCommit(false);

        long start = System.currentTimeMillis();
        int deleted = Employee.deleteAll();
        System.out.println("Deleted " + deleted  + " in " + (System.currentTimeMillis() - start) + " milliseconds");

        start = System.currentTimeMillis();
        for(int i = 0; i < 50000; i++){
            new Employee().set("name", "name: " + i).saveIt();
        }
        Base.connection().commit();
        System.out.println("Done in " + (System.currentTimeMillis() - start) + " milliseconds");
        Base.close();
    }
}
