package org.javalite.activejdbc;

import org.javalite.activejdbc.statistics.QueryStats;
import org.javalite.activejdbc.statistics.StatisticsQueue;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

import java.util.List;

/**
 * @author Igor Polevoy on 12/10/15.
 */
public class ShardingTest extends ActiveJDBCTest {

    @Test
    public void shouldAccessDifferentTablesFromSameModel() {

        deleteAndPopulateTable("people");
        Person p = new Person();
        p.set("name", "igor", "last_name", "polevoy").saveIt();
        p.refresh();
        List<Person> list = Person.where("name = 'John'").orderBy("dob desc");


        Person.getMetaModel().setShardTableName("person1");

        try {
            Person.where("name = 'John'").orderBy("dob desc").size();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
