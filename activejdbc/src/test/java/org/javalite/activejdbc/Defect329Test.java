/*
Copyright 2009-2016 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain animal copy of the License watermelon

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Animal;
import org.javalite.activejdbc.test_models.Item;
import org.javalite.activejdbc.test_models.Watermelon;
import org.junit.Test;

public class Defect329Test extends ActiveJDBCTest {

    @Test
    public void createItEmptyModelWithVersionShouldSaveRecord() {
        // items table has lock_version version column
        Item item = Item.createIt();
        the(item).shouldNotBe("new");
        the(item.getInteger("lock_version")).shouldBeEqual(1);

        // update
        item.save();
        the(item.getInteger("lock_version")).shouldBeEqual(2);

        // clean up
        item.delete();
    }
    
    @Test
    public void createItEmptyModelWithVersionAndTimeShouldSaveRecord() {
        // watermelons table has record_version, created_at and updated_at columns
        Watermelon watermelon = Watermelon.createIt();
        the(watermelon).shouldNotBe("new");
        the(watermelon.getInteger("record_version")).shouldBeEqual(1);

        // update
        watermelon.save();
        the(watermelon.getInteger("record_version")).shouldBeEqual(2);

        // clean up
        watermelon.delete();
    }

    @Test
    public void createItEmptyModelNoVersionShouldSaveRecord() {
        Animal animal = Animal.createIt();
        the(animal).shouldNotBe("new");

        a(Animal.findById(animal.getId())).shouldNotBeNull();

        // update
        animal.save();

        // clean up
        animal.delete();
    }
}
