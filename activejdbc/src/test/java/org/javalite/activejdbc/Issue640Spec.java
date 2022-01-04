package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Status;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.javalite.activejdbc.test.JdbcProperties.driver;

public class Issue640Spec  extends ActiveJDBCTest {

    @Before
    public void beforeTest(){

    }

    @Test
    public void shouldWorkWithPlainSQL(){
        if(driver().contains("postgresql")){
            Base.exec("insert into statuses (status) values (?::STATUS_TYPE)", "A");
            List<Map<String, Object>> list = Base.findAll("select * from  statuses");
            Object status = list.get(0).get("status");
            the(status).shouldBeEqual("A");
            the(status).shouldBeA(String.class);
        }
    }

    @Test
    public void shouldWorkWithModels(){
        if(driver().contains("postgresql")){
            Status s = new Status();
            s.set("status", "A");
            s.saveIt();
            List<Status> statuses = Status.findAll();
            the(statuses.size()).shouldBeEqual(1);
            the(statuses.get(0).get("status")).shouldBeEqual("A");
        }
    }
}
