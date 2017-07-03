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


package org.javalite.logging;

import org.javalite.common.JsonHelper;
import org.javalite.common.Util;
import org.javalite.test.SystemStreamUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;


/**
 * See log4j configuration in the log4j.properties file
 *
 * @author igor on 1/13/17.
 */
public class JsonLog4jLayoutSpec {

    @Before
    public void before(){
        SystemStreamUtil.replaceOut();
    }

    @After
    public void after() {
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldLogJson() throws IOException {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("hello");
        logger.info("world");
        String out = SystemStreamUtil.getSystemOut();
        String[] lines = Util.split(out, System.getProperty("line.separator"));
        the(lines.length).shouldBeEqual(2);
        String logLine1 = lines[0];
        Map log1 = JsonHelper.toMap(logLine1);
        a(log1.get("message")).shouldBeEqual("hello");
        a(log1.get("logger")).shouldBeEqual("org.javalite.logging.JsonLog4jLayoutSpec");
        a(log1.get("level")).shouldBeEqual("INFO");

        String logLine2 = lines[1];
        Map log2 = JsonHelper.toMap(logLine2);
        a(log2.get("message")).shouldBeEqual("world");

        String timestamp = (String) log1.get("timestamp");
        shouldParseTimestamp(timestamp);
    }

    private void shouldParseTimestamp(String timestamp){
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sf.parse(timestamp); // should not  generate exception
        } catch (ParseException e) {throw new RuntimeException(); }
    }
}
