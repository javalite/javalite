/*
Copyright 2009-2018 Igor Polevoy

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

package org.javalite.instrumentation;

/**
 * @author Andrey Yanchevsky
 */
public class Logger {

    private static boolean enableVerbose = System.getProperty("activejdbc-instrumentation.log") != null;

    private static Log log = new Log();

    static void setLog(Log l) {
        log = l;
    }

    public static void info(String s) {
        log.info("ActiveJDBC Instrumentation - " + s);
    }

    public static void error(String s) {
        log.error("ActiveJDBC Instrumentation - " + s);
    }

    public static void debug(String s) {
        if (enableVerbose) {
            info(s);
        }
    }
}
