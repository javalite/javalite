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


package org.javalite.activejdbc;

import org.javalite.common.Util;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import static org.javalite.test.jspec.JSpec.the;


public class BaseTest2 {

    @Test
    public void shouldOpenConnectionWithProperties() throws IOException {

        Properties props = Util.readProperties("/database.properties");
        DB db = new DB("test");
        the(db.hasConnection()).shouldBeFalse();

        String res = db.withDb(props.getProperty("development.test.driver"),
                props.getProperty("development.test.url"),
                props.getProperty("development.test.username"),
                props.getProperty("development.test.password"),
                () -> {
                            the(db.hasConnection()).shouldBeTrue();
                            return "hello";
                       });

        the(res).shouldBeEqual("hello");
        the(Base.hasConnection()).shouldBeFalse();
    }

    @Test
    public void shouldOpenConnectionWithJNDI() {

        DB db = new DB("test");

        String jndiName = "anyname"; // because we use MockInitialContextFactory, see jndi.properties file
        String res = db.withDb(jndiName, () -> {
            the(db.hasConnection()).shouldBeTrue();
            return  "hello";
        });

        the(db.hasConnection()).shouldBeFalse();
        the(res).shouldBeEqual("hello");
    }

    @Test
    public void shouldReuseExistingConnection() {

        DB db = new DB("test");

        String jndiName = "anyname"; // because we use MockInitialContextFactory, see jndi.properties file
        db.open(jndiName);
        Connection connection = db.connection();

        String res = db.withDb(jndiName, () -> {
            the(db.hasConnection()).shouldBeTrue();
            the(db.connection()).shouldBeTheSameAs(connection);
            return  "hello";
        });

        the(db.hasConnection()).shouldBeTrue();
        the(res).shouldBeEqual("hello");

        db.close();
    }
}
