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

package org.javalite.db_migrator;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.javalite.db_migrator.SpecBuilder.the;

public class MojoIntegrationEnvironmentsSpec extends AbstractIntegrationSpec {

    @Test @Ignore
    public void shouldRunInEnvironments() throws IOException, InterruptedException {
        String dir = "target/test-project-environments";
        // drop
        execute(dir, "db-migrator:drop");

        // create database
        String output = execute(dir, "db-migrator:create");

        the(output).shouldContain("Created database jdbc:mysql://localhost/test_project");
        the(output).shouldContain("jdbc:mysql://localhost/test_project_stage");
        the(output).shouldContain("BUILD SUCCESS");
    }
}
