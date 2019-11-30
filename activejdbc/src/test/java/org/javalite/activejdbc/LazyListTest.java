package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

/**
 * @author Igor Polevoy: 3/23/12 3:40 PM
 */
public class LazyListTest extends ActiveJDBCTest {

    @Test
    public void shouldGenerateSql(){
        System.out.println(Person.where("name = ?", "John").offset(200).limit(20).orderBy("name").toSql(true));
    }

    @Test
    public void shouldGenerateSqlWithParameters() {
        a(Person.where("name = ? AND last_name = ?", "John", "Doe").toSql(true)
                .endsWith(", with parameters: John, Doe")).shouldBeTrue();
    }

    @Test
    public void shouldBeEqual() {
        deleteAndPopulateTable("people");
        the(Person.findAll().equals(Person.findAll())).shouldBeTrue();
    }
//
//    @Test
//    public void shouldLoadList(){
//        SystemStreamUtil.replaceOut();
//        Person.where("name = ?", "John").load();
//        String out = SystemStreamUtil.getSystemOut();
//        SystemStreamUtil.restoreSystemOut();
//        the(out).shouldContain("Loading models");
//    }
}
