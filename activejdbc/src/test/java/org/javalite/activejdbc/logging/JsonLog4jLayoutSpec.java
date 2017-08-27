/*
Copyright 2009-2016 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.activejdbc.logging;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Animal;
import org.javalite.common.JsonHelper;
import org.javalite.common.Util;
import org.javalite.logging.Context;
import org.javalite.test.SystemStreamUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * This is an additional spec for class JsonLog4jLayout. It exists here because we
 * are testing output from models.
 *
 * @author igor on 1/18/17.
 */
public class JsonLog4jLayoutSpec  extends ActiveJDBCTest{

    @Before
    public void setup() throws Exception {
        SystemStreamUtil.replaceOut();
    }

    @After
    public void tearDown(){
        SystemStreamUtil.restoreSystemOut();
        Context.clear();
    }

    @Test
    public void shouldLogJson() throws IOException {

        Logger logger = LoggerFactory.getLogger(getClass());

        logger.info("hello");
        logger.error("world");
        List<String> lines = getLogLines();
        the(lines.size()).shouldBeEqual(2);
        String logLine1 = lines.get(0);
        Map log1 = org.javalite.common.JsonHelper.toMap(logLine1);
        a(log1.get("message")).shouldBeEqual("hello");
        a(log1.get("logger")).shouldBeEqual(getClass().getName());
        a(log1.get("level")).shouldBeEqual("INFO");

        String logLine2 = lines.get(1);
        Map log2 = org.javalite.common.JsonHelper.toMap(logLine2);
        a(log2.get("message")).shouldBeEqual("world");
        a(log2.get("level")).shouldBeEqual("ERROR");
    }

    private List<String> getLogLines(){
        String out = SystemStreamUtil.getSystemOut();
        return Arrays.asList(Util.split(out, System.getProperty("line.separator")));
    }

    private String getLastLine(){
        List<String> lines = getLogLines();
        return lines.get(lines.size() - 1);
    }

    @Test
    public void shouldPrintJsonLogValuesAndParams(){

        deleteAndPopulateTable("animals");

        Context.put("user", "joeschmoe", "user_id", "234", "email", "joe@schmoe.me");
        Animal.findById(1);

        String json = getLastLine();

        System.err.println(json);
        Map logMap = org.javalite.common.JsonHelper.toMap(json);
        Map message = (Map) logMap.get("message");

        a(message.get("sql")).shouldContain("SELECT * FROM animals WHERE animal_id = ?");

        List params = (List) message.get("params");

        a(params.size()).shouldBeEqual(1);
        a(params.get(0)).shouldBeEqual(1);

        Map context = (Map) logMap.get("context");

        the((context.get("user"))).shouldBeEqual("joeschmoe");
        the((context.get("user_id"))).shouldBeEqual("234");
        the((context.get("email"))).shouldBeEqual("joe@schmoe.me");
    }

    @Test
    public void shouldPrintJsonWithoutParams(){
        deleteAndPopulateTable("animals");

        Context.put("user", "joeschmoe", "user_id", "234", "email", "joe@schmoe.me");
        Animal.findAll().size();

        String json = getLastLine();
        Map logMap = org.javalite.common.JsonHelper.toMap(json);

        Map message = (Map) logMap.get("message");

        List params = (List) message.get("params");
        a(message.get("sql")).shouldBeEqual("SELECT * FROM animals");
        a(params.size()).shouldBeEqual(0);
        Map context = (Map) logMap.get("context");
        the((context.get("user"))).shouldBeEqual("joeschmoe");
        the((context.get("user_id"))).shouldBeEqual("234");
        the((context.get("email"))).shouldBeEqual("joe@schmoe.me");
    }

    @Test
    public void shouldPrintJsonWithoutParamsAndLogValues(){
        deleteAndPopulateTable("animals");

        Animal.findAll().size();

        String json = getLastLine();
        Map logMap = org.javalite.common.JsonHelper.toMap(json);
        Map message = (Map) logMap.get("message");
        List params = (List) message.get("params");
        a(message.get("sql")).shouldBeEqual("SELECT * FROM animals");
        a(params.size()).shouldBeEqual(0);
        Map context = (Map) logMap.get("context");
        a(context).shouldBeNull();
    }

    @Test
    public void shouldClearLogValues(){
        deleteAndPopulateTable("animals");
        Context.put("user", "joeschmoe", "user_id", "234", "email", "joe@schmoe.me");
        Animal.findAll().size();
        String log = getLastLine();
        a(log).shouldContain("joe@schmoe.me");

        Context.clear(); // <<--- clearing context
        Animal.findAll().size();
        log = getLastLine();
        a(log).shouldNotContain("joe@schmoe.me");
    }

    @Test
    public void shouldLogException(){
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.error("fire!", new RuntimeException("house on \n fire!"));
        String logLine = getLastLine();
        the(logLine).shouldContain("house on \\n fire!");
    }

    @Test
    public void shouldLogExceptionWithNewLineInMessage(){
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.error("fire!", new RuntimeException("I'm really confused by this!\nWhat do you mean?" ));
        the(getLastLine()).shouldContain("I'm really confused by this!\\nWhat do you mean?");
    }
}
