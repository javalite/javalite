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

package org.javalite.db_migrator;

import org.junit.Test;

import java.io.IOException;

import static org.javalite.db_migrator.SpecBuilder.the;

public class MojoIntegrationEnvironmentsSpec extends AbstractIntegrationSpec {

    @Test
    public void shouldRunInEnvironments() throws IOException, InterruptedException {
        String dir = "target/test-project-environments";
        // drop
        execute(dir, "db-migrator:drop", "-o");

        // create database
        String output = execute(dir, "db-migrator:create", "-o");
        the(output).shouldContain(String.format("BUILD SUCCESS"));
    }
}
