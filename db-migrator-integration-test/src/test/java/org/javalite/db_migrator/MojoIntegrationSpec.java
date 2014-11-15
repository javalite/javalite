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

import java.io.*;
import org.javalite.activejdbc.Base;
import org.javalite.common.Util;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

public class MojoIntegrationSpec extends JSpecSupport {

    @Test
    public void shouldRunTestProject() throws IOException, InterruptedException {
        run(new File("target/test-project"));
    }

    @Test
    public void shouldRunTestProjectWithProperties() throws IOException, InterruptedException {
        run(new File("target/test-project-properties"));
    }

    private void run(File dir) throws IOException, InterruptedException {
        String mvn = System.getProperty("os.name").startsWith("Windows") ? "mvn.bat" : "mvn";
        // drop
        execute(dir, mvn, "db-migrator:drop" , "-o");

        // create database
        String output = execute(dir, mvn, "db-migrator:create", "-o");
        the(output).shouldContain("[INFO] Created database jdbc:mysql://localhost/test_project");

        // migrate
        output = execute(dir, mvn, "db-migrator:migrate" , "-o");
        the(output).shouldContain(String.format("[INFO] Migrating database, applying 4 migration(s)%n" +
                "[INFO] Running migration 20080718214030_base_schema.sql%n" +
                "[INFO] Running migration 20080718214031_new_functions.sql%n" +
                "[INFO] Running migration 20080718214032_new_proceedure.sql%n" +
                "[INFO] Running migration 20080718214033_seed_data.sql"));

        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test_project", "root", "p@ssw0rd");
        a(Base.count("books")).shouldBeEqual(9);
        a(Base.count("authors")).shouldBeEqual(2);
        Base.close();

        // drop, create and validate
        output = execute(dir, mvn, "db-migrator:drop", "-o");
        the(output).shouldContain("[INFO] Dropped database jdbc:mysql://localhost/test_project");

        output = execute(dir, mvn, "db-migrator:create", "-o");
        the(output).shouldContain("[INFO] Created database jdbc:mysql://localhost/test_project");

        output = execute(dir, mvn, "db-migrator:validate", "-o");
        the(output).shouldContain(String.format("[INFO] Pending Migrations: %n" +
                "[INFO] 20080718214030_base_schema.sql%n" +
                "[INFO] 20080718214031_new_functions.sql%n" +
                "[INFO] 20080718214032_new_proceedure.sql%n" +
                "[INFO] 20080718214033_seed_data.sql"));

        // now migrate and validate again
        execute(dir, mvn, "db-migrator:migrate", "-o");

        output = execute(dir, mvn, "db-migrator:validate", "-o");
        the(output).shouldContain(String.format("[INFO] Database: jdbc:mysql://localhost/test_project%n" +
                "[INFO] Up-to-date: true%n" +
                "[INFO] No pending migrations found"));

        // creation of new migration
        execute(dir, mvn, "db-migrator:new", "-Dname=add_people", "-o");
        File migrationsDir = new File(dir, "src/migrations");
        String migrationFile = findMigrationFile(migrationsDir, "add_people");
        the(migrationFile).shouldNotBeNull();
        the(new File(migrationsDir, migrationFile).delete()).shouldBeTrue();
    }

    private static String execute(File dir, String... args) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(args, null, dir);
        p.waitFor();
        String out = Util.read(p.getInputStream());
        String err = Util.read(p.getErrorStream());
        String output = "TEST MAVEN EXECUTION START >>>>>>>>>>>>>>>>>>>>>>>>\nOut: \n" + out
                + "\nErr:" + err + "\nTEST MAVEN EXECUTION END <<<<<<<<<<<<<<<<<<<<<<";
        if (p.exitValue() != 0) {
            System.out.println(output);
        }
        return output;
    }

    //will return null of not found
    private static String findMigrationFile(File dir, String substring) {
        String[] files = dir.list();
        for (String file : files) {
            if (file.contains(substring)) {
                return file;
            }
        }
        return null;
    }
}
