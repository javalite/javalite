/*
Copyright 2009-2019 Igor Polevoy

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

package org.javalite.db_migrator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.javalite.common.RuntimeUtil;
import org.javalite.common.Util;
import org.junit.BeforeClass;

import static org.javalite.common.Util.blank;

abstract class AbstractIntegrationSpec {

    private static final String MVN = SystemUtils.IS_OS_WINDOWS ? "mvn.cmd " : "mvn ";
    private static String profileId = System.getProperty("profileId");

    @BeforeClass
    public static void before(){
        //lets install the parent pom
        execute("src/test/project", "install", "-N");
    }

    static String execute(String dir, String... args) {

        List<String> argsList = new ArrayList<>(Arrays.asList(args));


        String  mavenArgs = blank(profileId) ? Util.join(argsList, " ") : (Util.join(argsList, " ") + "  -P" + profileId);

        System.out.println("\n\nTEST MAVEN EXECUTION START >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("Executing: " + dir + " $> " + MVN + mavenArgs);
        RuntimeUtil.Response response = RuntimeUtil.execute(8096, new File(dir), MVN + mavenArgs);

        String out = response.out;
        String err = response.err;

        System.out.println();
        System.out.print("Exit code: ");
        System.out.println(response.exitValue);
        System.out.println("************ STDOUT ***********");
        System.out.print(response.out);
        System.err.println(response.err);
        System.out.println("************ STDERR ***********");
        System.out.println("TEST MAVEN EXECUTION END <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n\n");

        return out + err;
    }

    String getMariaDBHost(){
        return !blank(profileId) && profileId.equals("jenkins")? "mariadb" : "localhost";
    }

    public boolean isJenkins(){
        return profileId.equals("jenkins");
    }
}
