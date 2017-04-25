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
 * @author Igor Polevoy: 12/17/13 3:05 PM
 */

package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class BatchExecTest extends ActiveJDBCTest {

    @Before
    public void setup() throws Exception {
        deleteFromTable("people");
    }

    @Test
    public void shouldInsertAsBatch() {

        PreparedStatement ps = Base.startBatch("insert into people (NAME, LAST_NAME, DOB) values(?, ?, ?)");

        Base.addBatch(ps, "Mic", "Jagger", getDate(1962, 1, 1));
        Base.addBatch(ps, "Marilyn", "Monroe", getDate(1932, 1, 1));
        int[] counts = Base.executeBatch(ps);

        the(counts.length).shouldBeEqual(2);
        the(counts[0] == 1 || counts[0] == Statement.SUCCESS_NO_INFO).shouldBeTrue(); //Oracle!!
        the(counts[1] == 1 || counts[1] == Statement.SUCCESS_NO_INFO).shouldBeTrue();

        List<Map> people = Base.findAll("select * from people order by name");

        the(people.size()).shouldBeEqual(2);
        the(people.get(0).get("name")).shouldBeEqual("Marilyn");
        the(people.get(1).get("name")).shouldBeEqual("Mic");
    }
}
