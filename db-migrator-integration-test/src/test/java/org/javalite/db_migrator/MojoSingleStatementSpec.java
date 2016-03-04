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

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.javalite.db_migrator.DbUtils.*;
import static org.javalite.db_migrator.SpecBuilder.the;
import static org.junit.Assert.*;

public class MojoSingleStatementSpec extends AbstractIntegrationSpec {

    @Test
    public void shouldRunTestProjectWithSingleStatement() throws IOException, InterruptedException {
        run("target/test-project-single-statement");
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

        openConnection("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test_project", "root", "p@ssw0rd");
        assertEquals(countRows("books"), 1);
        closeConnection();
    }
}
