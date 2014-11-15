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

package org.javalite.db_migrator;

import java.io.*;
import org.junit.Test;

public class MojoIntegrationEnvironmentsSpec extends AbstractIntegrationSpec {

    @Test
    public void shouldRunInEnvironments() throws IOException, InterruptedException {
        File dir = new File("target/test-project-environments");
        String mvn = System.getProperty("os.name").startsWith("Windows") ? "mvn.bat" : "mvn";
        // drop
        execute(dir, mvn, "db-migrator:drop" , "-o");

        // create database
        String output = execute(dir, mvn, "db-migrator:create", "-o");
        the(output).shouldContain(String.format("[INFO] Environment: development%n" 
                + "[INFO] Created database jdbc:mysql://localhost/test_project_devel%n" 
                + "[INFO] Environment: staging%n" 
                + "[INFO] Created database jdbc:mysql://localhost/test_project_stage"));

    }
}
