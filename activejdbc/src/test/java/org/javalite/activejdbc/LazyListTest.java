package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

/**
 * @author Igor Polevoy: 3/23/12 3:40 PM
 */
public class LazyListTest extends ActiveJDBCTest {

    @Test
    public void shouldGenerateSql(){
        System.out.println(Person.where("name = ?", "John").offset(200).limit(20).orderBy("name").toSql(true));
    }
}
