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

/**
 * @author Igor Polevoy: 12/28/13 1:37 PM
 */

package org.javalite.db_migrator;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.javalite.db_migrator.JdbcPropertiesOverride.*;
import static org.junit.Assert.*;
import static org.javalite.db_migrator.DbUtils.*;
import static org.javalite.db_migrator.SpecBuilder.the;

public class MojoIntegrationSpec extends AbstractIntegrationSpec {

    @Test
    public void shouldRunTestProject() throws IOException, InterruptedException {
        run("target/test-project");
    }

    @Test
    public void shouldRunTestProjectWithProperties() throws IOException, InterruptedException {
        run("target/test-project-properties");
    }

    private void run(String dir) throws IOException, InterruptedException {
        // drop
        execute(dir, "db-migrator:drop", "-o");

        // create database
        String output = execute(dir, "db-migrator:create", "-o");
        the(output).shouldContain("Created database jdbc:mysql://localhost/test_project");
        the(output).shouldContain("BUILD SUCCESS");

        // migrate
        output = execute(dir, "db-migrator:migrate", "-o");
        the(output).shouldContain("BUILD SUCCESS");

        openConnection(driver(), "jdbc:mysql://localhost/test_project", user(), password());
        assertEquals(countRows("books"), 9);
        assertEquals(countRows("authors"), 2);
        closeConnection();

        // drop, create and validate
        output = execute(dir, "db-migrator:drop", "-o");
        the(output).shouldContain("Dropped database jdbc:mysql://localhost/test_project");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(dir, "db-migrator:create", "-o");
        the(output).shouldContain("Created database jdbc:mysql://localhost/test_project");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(dir, "db-migrator:validate", "-o");
        the(output).shouldContain("BUILD SUCCESS");

        // now migrate and validate again
        output = execute(dir, "db-migrator:migrate", "-o");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(dir, "db-migrator:validate", "-o");
        the(output).shouldContain("No pending migrations found");
        the(output).shouldContain("BUILD SUCCESS");

        // creation of new migration
        execute(dir, "db-migrator:new", "-Dname=add_people", "-o");
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
