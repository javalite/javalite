package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Status;
import org.javalite.json.JSONMap;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.javalite.activejdbc.test.JdbcProperties.driver;

public class Issue640Spec  extends ActiveJDBCTest {

    @Before
    public void start(){
        Assume.assumeTrue(driver().contains("postgresql"));
    }

    @Test
    public void shouldWorkWithPlainSQL(){
            Base.exec("insert into statuses (status) values (?::STATUS_TYPE)", "A");
            List<Map<String, Object>> list = Base.findAll("select * from  statuses");
            Object status = list.get(0).get("status");
            the(status).shouldBeEqual("A");
            the(status).shouldBeA(String.class);
    }

    @Test
    public void shouldTestInsertUpdateWithModels(){


            //testing INSERT:
            Status s = new Status();
            s.set("status", "A");
            s.saveIt();

            List<Status> statuses = Status.findAll();
            the(statuses.size()).shouldBeEqual(1);
            the(statuses.get(0).get("status")).shouldBeEqual("A");
            the(statuses.get(0).get("description")).shouldBeNull();

            //testing UPDATE
            s.set("description", "test abc");
            s.set("status", "B").saveIt();
            statuses = Status.findAll();

            the(statuses.size()).shouldBeEqual(1);
            the(statuses.get(0).get("status")).shouldBeEqual("B");
            the(statuses.get(0).get("description")).shouldBeEqual("test abc");

            //testing DELETE:
            Status.delete("status = ?::status_type","B" ); // need to provide type explicitly
            the(Status.count()).shouldEqual(0);

    }


    @Test
    public void shouldTestJSONB(){
        if(driver().contains("postgresql")){

            JSONMap m = new JSONMap("index", 1, "value", "earth");
            //testing INSERT:
            Status s = new Status();
            s.set("status", "C");
            s.setJSONMap("data", m);
            s.saveIt();

            Status s1 = (Status) Status.findAll().get(0);
            the(s1.getJSONMap("data")).shouldEqual(m);

            //testing UPDATE:
            m.put("value", "moon");
            s.setJSONMap("data", m).saveIt();

            Status s2 = (Status) Status.findAll().get(0);
            the(s2.getJSONMap("data")).shouldEqual(m);
        }
    }
}
