/*
Copyright 2009-2010 Igor Polevoy 

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.javalite.db_migrator.SpecHelper.*;
import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

public class MojoIntegrationSpec {

    @Test
    public void shouldRunEntireIntegrationSpec() throws IOException, InterruptedException {

        reCreateProject();

        //drop
        execute("mvn", "db-migrator:drop");

        //create database
        reCreateProject();
        execute("mvn", "db-migrator:create");

        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test_project", "root", "p@ssw0rd");

        //creation of new migration
        reCreateProject();
        execute("mvn", "db-migrator:new", "-Dname=add_people");
        a(findMigrationFile("add_people")).shouldNotBeNull();

        //migrate
        reCreateProject();
        String output = execute("mvn", "db-migrator:migrate");



        the(output ).shouldContain("[INFO] Migrating jdbc:mysql://localhost/test_project using migrations at src/migrations/\n" +
                "[INFO] Migrating database, applying 4 migration(s)\n" +
                "[INFO] Running migration 20080718214030_base_schema.sql\n" +
                "[INFO] Running migration 20080718214031_new_functions.sql\n" +
                "[INFO] Running migration 20080718214032_new_proceedure.sql\n" +
                "[INFO] Running migration 20080718214033_seed_data.sql");

        a(Base.count("books")).shouldBeEqual(9);
        a(Base.count("authors")).shouldBeEqual(2);

        //validate
        reCreateProject();
        execute("mvn", "db-migrator:drop");
        execute("mvn", "db-migrator:create");
        output = execute("mvn", "db-migrator:validate");

        the(output).shouldContain("[INFO] Pending Migrations: \n" +
                "[INFO] 20080718214030_base_schema.sql\n" +
                "[INFO] 20080718214031_new_functions.sql\n" +
                "[INFO] 20080718214032_new_proceedure.sql\n" +
                "[INFO] 20080718214033_seed_data.sql");
        //now migrate and validate again

        output = execute("mvn", "db-migrator:migrate");

        output = execute("mvn", "db-migrator:validate");

        System.out.println(output);

        the(output).shouldContain("[INFO] Validating jdbc:mysql://localhost/test_project using migrations at src/migrations/\n" +
                                    "[INFO] Database: jdbc:mysql://localhost/test_project\n" +
                                    "[INFO] Up-to-date: true\n" +
                                    "[INFO] No pending migrations found");

        Base.close();
    }


}
