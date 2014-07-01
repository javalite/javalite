/*
Copyright 2009-2014 Igor Polevoy

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

/**
 * @author Igor Polevoy: 12/28/13 1:37 PM
 */

package org.javalite.db_migrator;

import org.codehaus.plexus.util.FileUtils;
import org.javalite.activejdbc.Base;
import org.javalite.common.Util;
import org.junit.Test;

import java.io.*;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

public class MojoIntegrationSpec {

    public static final String TEST_PROJECT_DIR = "target/test-project";

    @Test
    public void shouldRunEntireIntegrationSpec() throws IOException, InterruptedException {

        reCreateProject();

        //drop
        execute("mvn", "db-migrator:drop" , "-o");

        //create database
        reCreateProject();
        execute("mvn", "db-migrator:create", "-o");

        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test_project", "root", "p@ssw0rd");

        //creation of new migration
        reCreateProject();
        String output = execute("mvn", "db-migrator:new", "-Dname=add_people", "-o");

        a(findMigrationFile("add_people")).shouldNotBeNull();

        //migrate
        reCreateProject();
        output = execute("mvn", "db-migrator:migrate" , "-o");

        the(output).shouldContain("[INFO] Migrating database, applying 4 migration(s)\n" +
                "[INFO] Running migration 20080718214030_base_schema.sql\n" +
                "[INFO] Running migration 20080718214031_new_functions.sql\n" +
                "[INFO] Running migration 20080718214032_new_proceedure.sql\n" +
                "[INFO] Running migration 20080718214033_seed_data.sql");

        a(Base.count("books")).shouldBeEqual(9);
        a(Base.count("authors")).shouldBeEqual(2);

        //validate
        reCreateProject();
        execute("mvn", "db-migrator:drop", "-o");
        execute("mvn", "db-migrator:create", "-o");
        output = execute("mvn", "db-migrator:validate", "-o");

        the(output).shouldContain("[INFO] Pending Migrations: \n" +
                "[INFO] 20080718214030_base_schema.sql\n" +
                "[INFO] 20080718214031_new_functions.sql\n" +
                "[INFO] 20080718214032_new_proceedure.sql\n" +
                "[INFO] 20080718214033_seed_data.sql");
        //now migrate and validate again

        execute("mvn", "db-migrator:migrate", "-o");

        output = execute("mvn", "db-migrator:validate", "-o");

        the(output).shouldContain("[INFO] Database: jdbc:mysql://localhost/test_project\n" +
                "[INFO] Up-to-date: true\n" +
                "[INFO] No pending migrations found");

        Base.close();
    }


    //// UTILITY METHODS BELOW

    public static void reCreateProject() throws IOException {
        FileUtils.deleteDirectory(TEST_PROJECT_DIR);
        copyFolder(new File("src/test/test-project"), new File("target/test-project"));
    }

    public static void copyFolder(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists())
                dest.mkdir();

            for (String file : src.list())
                copyFolder(new File(src, file), new File(dest, file));
        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0)
                out.write(buffer, 0, length);

            in.close();
            out.close();
        }
    }

    public static String execute(String... args) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(args, null, new File(TEST_PROJECT_DIR));
        p.waitFor();
        String out = Util.read(p.getInputStream());
        String err = Util.read(p.getInputStream());
        String output = "TEST MAVEN EXECUTION START >>>>>>>>>>>>>>>>>>>>>>>>\nOut: \n" + out + "\nErr:" + err + "\nTEST MAVEN EXECUTION END <<<<<<<<<<<<<<<<<<<<<<";
        if(p.exitValue() != 0){
            System.out.println(output);
        }
        return output;
    }

    //will return null of not found
    public static String findMigrationFile(String substring) {
        String[] files = new File(TEST_PROJECT_DIR + "/src/migrations").list();
        for (String file : files) {
            if (file.contains(substring))
                return file;
        }
        return null;
    }
}
