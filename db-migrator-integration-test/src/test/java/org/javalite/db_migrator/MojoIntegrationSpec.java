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

/**
 * @author Igor Polevoy: 12/28/13 1:37 PM
 */

package org.javalite.db_migrator;

import org.javalite.activejdbc.Base;
import org.junit.Test;

import java.io.File;
import static org.javalite.db_migrator.JdbcPropertiesOverride.*;
import static org.javalite.test.jspec.JSpec.the;
import static org.junit.Assert.*;


public class MojoIntegrationSpec extends AbstractIntegrationSpec {


    @Test
    public void shouldRunTestProject() {
        run("src/test/project/test-project", "The book is: Hello, Book A!");
    }

    @Test
    public void shouldRunTestProjectWithProperties() {
        run("src/test/project/test-project-properties", null);
    }

    private void run(String dir, String val){
        // drop
        execute(dir, "db-migrator:drop");

        // create database
        String output = execute(dir, "db-migrator:create");

        String host = getMariaDBHost();

        the(output).shouldContain("Created database jdbc:mysql://" + host + "/test_project");
        the(output).shouldContain("BUILD SUCCESS");

        // migrate
        output = execute(dir, "db-migrator:migrate");
        the(output).shouldContain("BUILD SUCCESS");

        if(val != null){
            the(output).shouldContain(val);
        }

        Base.open(driver(), "jdbc:mysql://" + host +  "/test_project", user(), password());
        the(Base.count("books")).shouldBeEqual(9);
        the(Base.count("authors")).shouldBeEqual(2);
        Base.close();

        // drop, create and validate
        output = execute(dir, "db-migrator:drop");
        the(output).shouldContain("Dropped database jdbc:mysql://" + host +  "/test_project");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(dir, "db-migrator:create");
        the(output).shouldContain("Created database jdbc:mysql://" + host +  "/test_project");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(dir, "db-migrator:validate");
        the(output).shouldContain("BUILD SUCCESS");

        // now migrate and validate again
        output = execute(dir, "db-migrator:migrate");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(dir, "db-migrator:validate");
        the(output).shouldContain("No pending migrations found");
        the(output).shouldContain("BUILD SUCCESS");

        // creation of new migration
        execute(dir, "db-migrator:new", "-Dname=add_people");
        File migrationsDir = new File(dir, "src/migrations");
        String migrationFile = findMigrationFile(migrationsDir, "add_people");
        assertNotNull(migrationFile);
        assertTrue(new File(migrationsDir, migrationFile).delete());
    }

    // will return null of not found
    private String findMigrationFile(File dir, String substring) {
        String[] files = dir.list();
        for (String file : files) {
            if (file.contains(substring)) {
                return file;
            }
        }
        return null;
    }
}
