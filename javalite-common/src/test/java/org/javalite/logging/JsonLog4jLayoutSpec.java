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
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;


/**
 * See log4j configuration in the log4j.properties file
 *
 * @author igor on 1/13/17.
 */
public class JsonLog4jLayoutSpec {

    private static final String LOG_FILE="target/javalite.log";

    @Before
    public void before() throws IOException {
        try { Files.delete(Paths.get(LOG_FILE)); } catch (NoSuchFileException ignore) {}
    }

    @Test
    public void shouldLogJson() throws IOException {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("hello");
        logger.info("world");
        List<String> lines = Files.readAllLines(Paths.get(LOG_FILE));
        the(lines.size()).shouldBeEqual(2);
        String logLine1 = lines.get(0);
        Map log1 = JsonHelper.toMap(logLine1);
        a(log1.get("message")).shouldBeEqual("hello");
        a(log1.get("logger")).shouldBeEqual("org.javalite.logging.JsonLog4jLayoutSpec");
        a(log1.get("level")).shouldBeEqual("INFO");

        String logLine2 = lines.get(1);
        Map log2 = JsonHelper.toMap(logLine2);
        a(log2.get("message")).shouldBeEqual("world");

    }
}
