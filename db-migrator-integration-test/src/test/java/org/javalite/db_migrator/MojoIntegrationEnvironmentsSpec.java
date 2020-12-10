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

import org.junit.Test;


import static org.javalite.common.Util.blank;
import static org.javalite.test.jspec.JSpec.the;

public class MojoIntegrationEnvironmentsSpec extends AbstractIntegrationSpec {

    @Test
    public void shouldRunInEnvironments(){
        String dir = "src/test/project/test-project-environments";
        // drop
        execute(dir, "db-migrator:drop");

        // create database
        String output = execute(dir, "db-migrator:create");

        String host = getMariaDBHost();

        the(output).shouldContain("Created database jdbc:mysql://" + host + "/test_project");
        the(output).shouldContain("jdbc:mysql://" + host + "/test_project_stage");
        the(output).shouldContain("BUILD SUCCESS");
    }
}
