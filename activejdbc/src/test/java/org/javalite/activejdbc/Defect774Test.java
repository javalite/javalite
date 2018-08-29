package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.User;
import org.junit.Test;

/**
 * @author igor on 8/28/18.
 */
public class Defect774Test extends ActiveJDBCTest {

    @Test
    public void shouldNotProduceNPE(){
        Paginator p = new Paginator<>(User.class, 20, "SELECT p.* FROM users p JOIN addresses c ON " +
                "p.id=c.user_id WHERE p.first_name LIKE ? AND c.address1 IN (?, ?) GROUP BY p.id HAVING COUNT(p.id) = ?",
                "W%", "Alex", "John", 2)
                .orderBy("p.updated_at DESC");

        p.pageCount();
    }
}
