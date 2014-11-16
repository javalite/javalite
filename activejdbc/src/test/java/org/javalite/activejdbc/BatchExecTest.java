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
 * @author Igor Polevoy: 12/17/13 3:05 PM
 */

package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.junit.Test;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

public class BatchExecTest extends ActiveJDBCTest {
        @Override
        public void before() throws Exception {
            super.before();
            deleteFromTable("people");
        }

        @Test
        public void shouldInsertAsBatch() {

            PreparedStatement ps = Base.startBatch("insert into people (NAME, LAST_NAME, DOB) values(?, ?, ?)");

            Base.addBatch(ps, "Mic", "Jagger", new Date(getTime(1962, 1, 1)));
            Base.addBatch(ps, "Marilyn", "Monroe", new Date(getTime(1932, 1, 1)));
            Base.executeBatch(ps);

            List<Map> people = Base.findAll("select * from people order by name");

            the(people.size()).shouldBeEqual(2);
            the(people.get(0).get("name")).shouldBeEqual("Marilyn");
            the(people.get(1).get("name")).shouldBeEqual("Mic");
        }
}
