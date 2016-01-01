package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Interval;
import org.junit.Test;

import static org.javalite.activejdbc.test.JdbcProperties.url;
import static org.javalite.test.jspec.JSpec.$;

/**
 * @author Igor Polevoy on 1/1/16.
 */
public class Defect222Test extends ActiveJDBCTest {

    @Test
    public void test(){
        if(url().contains("mysql")){
            Interval interval = new Interval();
            interval.set("begin", 1, "end", 2).saveIt();
            $(Interval.count()).shouldBeEqual(1);
        }
    }
}
