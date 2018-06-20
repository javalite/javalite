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


package org.javalite.activejdbc;

import org.javalite.common.Util;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.javalite.test.jspec.JSpec.the;


public class BaseTest2 {

    @Test
    public void shouldOpenConnection() throws IOException {

        Properties props = Util.readProperties("/database.properties");

        DB db = new DB("test");
        the(db.hasConnection()).shouldBeFalse();

        System.out.println(props);
        db.withDb(props.getProperty("development.test.driver"), props.getProperty("development.test.url"),
                props.getProperty("development.test.username"), props.getProperty("development.test.password"), () -> {

            the(db.hasConnection()).shouldBeTrue();

        });

        the(Base.hasConnection()).shouldBeFalse();
    }
}
