/*
 * Copyright 2015 JavaLite.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javalite.activejdbc;

import java.util.HashMap;
import java.util.Map;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Programmer;
import org.junit.Test;

/**
 *
 * @author Marcin Pikulski
 */
public class Defect381_UpdateModifiedOnlyTest extends ActiveJDBCTest {

    @Test
    public void shouldBeModifiedWhenModified() {
        Programmer prg = Programmer.createIt("first_name", "John");
        the(prg).shouldNotBe("new");
        the(prg).shouldNotBe("modified");
        prg.set("last_name", "Doe");
        the(prg).shouldBe("modified");
        the(prg.dirtyAttributeNames()).shouldContain("last_name");
        the(prg.dirtyAttributeNames().size()).shouldBeEqual(1);
        
        prg.saveIt();
        the(prg).shouldNotBe("modified");
        the(prg.dirtyAttributeNames().size()).shouldBeEqual(0);
    }
    
    @Test
    public void shouldBeModifiedAfterCreated() {
        Programmer prg = Programmer.create("first_name", "John");
        the(prg).shouldBe("new");
        the(prg).shouldBe("modified");
        prg.set("last_name", "Doe");
        the(prg).shouldBe("modified");
        the(prg.dirtyAttributeNames()).shouldBeEqual(prg.getAttributes().keySet());
        
        prg.saveIt();
        the(prg).shouldNotBe("modified");
        the(prg.dirtyAttributeNames().size()).shouldBeEqual(0);
    }
    
    @Test
    public void shouldBeModifiedAfterFromMap() {
        Programmer prg = Programmer.createIt("first_name", "John");
        the(prg).shouldNotBe("new");
        the(prg).shouldNotBe("modified");

        Map<String, Object> map = new HashMap<>();
        map.put("last_name", "Doe");
        prg.fromMap(map);
        the(prg).shouldBe("modified");
        prg.saveIt();
        the(prg).shouldNotBe("modified");
    }
    
    @Test
    public void shouldNotBeModifiedAfterFind() {
        Programmer prg = Programmer.createIt("first_name", "John", "last_name", "Doe");
        the(prg).shouldNotBe("new");
        the(prg).shouldNotBe("modified");
        prg.set("last_name", "Roe");
        
        Programmer prgFromDB = Programmer.findById(prg.getId());
        the(prgFromDB).shouldNotBe("new");
        the(prgFromDB).shouldNotBe("modified");
        the(prgFromDB.get("last_name")).shouldBeEqual("Doe");
        
        prgFromDB.set("first_name", "Jane");
        prgFromDB.set("last_name", "Moe");
        the(prgFromDB).shouldBe("modified");
        the(prgFromDB.dirtyAttributeNames()).shouldContain("first_name");
        the(prgFromDB.dirtyAttributeNames()).shouldContain("last_name");
        the(prgFromDB.dirtyAttributeNames()).shouldContain("First_Name");
        the(prgFromDB.dirtyAttributeNames()).shouldContain("LAST_NAME");
        the(prgFromDB.dirtyAttributeNames().size()).shouldBeEqual(2);
        
        prgFromDB.saveIt();
        the(prgFromDB).shouldNotBe("modified");
        
        prgFromDB = (Programmer) Programmer.where("id = ?", prg.getId()).get(0);
        the(prgFromDB).shouldNotBe("new");
        the(prgFromDB).shouldNotBe("modified");
    }
    
    @Test
    public void shouldBeModifiedAfterDeletaAndThaw() {
        Programmer prg = Programmer.createIt("first_name", "John", "last_name", "Doe");
        the(prg).shouldNotBe("new");
        the(prg).shouldNotBe("modified");
        prg.delete();
        the(prg).shouldBe("frozen");
        the(prg).shouldNotBe("modified");
        
        prg.thaw();
        the(prg).shouldBe("new");
        the(prg).shouldBe("modified");
        the(prg.dirtyAttributeNames()).shouldBeEqual(prg.getAttributes().keySet());
        
        prg.saveIt();
        the(prg).shouldNotBe("new");
        the(prg).shouldNotBe("modified");
    }
    
    @Test
    public void shouldBeModifiedAfterCopyTo() {
        Programmer prg = Programmer.createIt("first_name", "John", "last_name", "Doe");
        the(prg).shouldNotBe("new");
        the(prg).shouldNotBe("modified");
        
        Programmer prg2 = Programmer.createIt("first_name", "Jane", "last_name", "Doe");
        the(prg).shouldNotBe("new");
        the(prg).shouldNotBe("modified");
        
        prg.copyTo(prg2);
        the(prg).shouldNotBe("new");
        the(prg2).shouldBe("modified");
    }
    
    @Test
    public void shouldBeCaseInsensitive() {
        Programmer prg = new Programmer();
        prg.set("FIRST_NAME", "John");
        prg.set("First_Name", "John");
        prg.set("first_name", "John");
        the(prg.dirtyAttributeNames().size()).shouldBeEqual(1);
    }
}
