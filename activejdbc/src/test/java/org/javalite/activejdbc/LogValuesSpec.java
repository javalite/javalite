package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Animal;
import org.javalite.test.SystemStreamUtil;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author igor on 1/6/17.
 */
@Ignore
public class LogValuesSpec extends ActiveJDBCTest {


    @AfterClass
    public static void afterTest(){
        LogFilter.clearLogValues();
        SystemStreamUtil.restoreSystemErr();
    }

    @Test
    public void shouldPrintJsonLogValuesAndParams(){
        deleteAndPopulateTable("animals");

        SystemStreamUtil.replaceError();
        LogFilter.addLogValues("user", "joeschmoe", "user_id", "234", "email", "joe@schmoe.me");
        Animal.findById(1);

        String log = SystemStreamUtil.getSystemErr();
        SystemStreamUtil.restoreSystemErr();

        System.out.println(log);
        //Lets test what was logged.
        String[] parts = log.split("LazyList -");
        String json = parts[1];
        Map logMap =JsonHelper.toMap(json);
        List params = (List) logMap.get("params");
        a(logMap.get("query")).shouldBeEqual("SELECT * FROM animals WHERE animal_id = ? LIMIT 1");
        a(params.size()).shouldBeEqual(1);
        a(params.get(0)).shouldBeEqual("1");
        Map logValues = (Map) logMap.get("log_values");
        the((logValues.get("user"))).shouldBeEqual("joeschmoe");
        the((logValues.get("user_id"))).shouldBeEqual("234");
        the((logValues.get("email"))).shouldBeEqual("joe@schmoe.me");
    }

    @Test
    public void shouldPrintJsonWithoutParams(){
        deleteAndPopulateTable("animals");

        SystemStreamUtil.replaceError();
        LogFilter.addLogValues("user", "joeschmoe", "user_id", "234", "email", "joe@schmoe.me");
        Animal.findAll().size();

        String log = SystemStreamUtil.getSystemErr();
        SystemStreamUtil.restoreSystemErr();

        //Lets test what was logged.
        String[] parts = log.split("LazyList -");

        String json = parts[1];
        Map logMap =JsonHelper.toMap(json);
        List params = (List) logMap.get("params");
        a(logMap.get("query")).shouldBeEqual("SELECT * FROM animals");
        a(params).shouldBeNull();
        Map logValues = (Map) logMap.get("log_values");
        the((logValues.get("user"))).shouldBeEqual("joeschmoe");
        the((logValues.get("user_id"))).shouldBeEqual("234");
        the((logValues.get("email"))).shouldBeEqual("joe@schmoe.me");
    }

    @Test
    public void shouldPrintJsonWithoutParamsAndLogValues(){
        deleteAndPopulateTable("animals");

        SystemStreamUtil.replaceError();
        LogFilter.clearLogValues();
        Animal.findAll().size();

        String log = SystemStreamUtil.getSystemErr();
        SystemStreamUtil.restoreSystemErr();

        //Lets test what was logged.
        String[] parts = log.split("LazyList -");

        String json = parts[1];
        Map logMap =JsonHelper.toMap(json);
        List params = (List) logMap.get("params");
        a(logMap.get("query")).shouldBeEqual("SELECT * FROM animals");
        a(params).shouldBeNull();
        Map logValues = (Map) logMap.get("log_values");

        a(logValues).shouldBeNull();
    }

    @Test
    public void shouldClearLogValues(){
        deleteAndPopulateTable("animals");

        SystemStreamUtil.replaceError();

        LogFilter.addLogValues("user", "joeschmoe", "user_id", "234", "email", "joe@schmoe.me");
        Animal.findAll().size();

        String log = SystemStreamUtil.getSystemErr();
        SystemStreamUtil.restoreSystemErr();

        a(log).shouldContain("joe@schmoe.me");

        SystemStreamUtil.replaceError();
        LogFilter.clearLogValues();
        Animal.findAll().size();

        log = SystemStreamUtil.getSystemErr();
        SystemStreamUtil.restoreSystemErr();

        a(log).shouldNotContain("joe@schmoe.me");
    }
}
