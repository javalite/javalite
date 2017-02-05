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
package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.junit.Test;

import java.util.List;

/**
 *  This class exists to test the override of parent_type values with annotations
 *
 *
 * @author Igor Polevoy
 */
public class PolymorphicAssociationsOverrideTest extends ActiveJDBCTest {


    @Test
    public void shouldAddPolymorphicChild() {
        deleteAndPopulateTables("vehicles", "mammals", "classifications");
        Vehicle car = Vehicle.createIt("name", "car");
        Classification classification = Classification.create("name", "four wheeled");
        car.add(classification);
        a(classification.get("parent_type")).shouldBeEqual("Vehicle");
        a(Classification.findAll().get(0).get("name")).shouldBeEqual("four wheeled");
    }

    @Test
    public void shouldFindAllPolymorphicChildren() {
        deleteAndPopulateTables("vehicles", "mammals", "classifications");
        Vehicle car = Vehicle.createIt("name", "car");
        car.add(Classification.create("name", "four wheeled"));
        car.add(Classification.create("name", "sedan"));

        List<Classification> classifications = car.getAll(Classification.class).orderBy("id");

        a(classifications.size()).shouldBeEqual(2);
        a(classifications.get(1).get("name")).shouldBeEqual("sedan");
//

        Mammal fox = Mammal.createIt("name", "fox");
        fox.add(Classification.create("name", "furry"));
        fox.add(Classification.create("name", "carnivore"));
        fox.add(Classification.create("name", "four legged"));

        classifications = fox.getAll(Classification.class).orderBy("id");
        a(classifications.size()).shouldBeEqual(3);

        a(classifications.get(0).get("name")).shouldBeEqual("furry");
        a(classifications.get(1).get("name")).shouldBeEqual("carnivore");
        a(classifications.get(2).get("name")).shouldBeEqual("four legged");

        a(Classification.findAll().size()).shouldBeEqual(5);
    }

    @Test
    public void shouldFindAllPolymorphicChildrenWithCriteria() {

        deleteAndPopulateTables("vehicles", "mammals", "classifications");
        Vehicle car = Vehicle.createIt("name", "car");
        car.add(Classification.create("name", "four wheeled"));
        car.add(Classification.create("name", "sedan"));

        List<Classification> classifications = car.get(Classification.class, "name = ?", "sedan");
        a(classifications.size()).shouldBeEqual(1);
        a(classifications.get(0).get("name")).shouldBeEqual("sedan");
    }

    @Test
    public void shouldRemovePolymorphicChildren() {
        deleteAndPopulateTables("vehicles", "mammals", "classifications");

        Vehicle car = Vehicle.createIt("name", "car");
        Classification fourWheels = (Classification)Classification.create("name", "four wheeled");
        car.add(fourWheels);
        car.add(Classification.create("name", "sedan"));

        a(Classification.count()).shouldBeEqual(2);
        a(car.remove(fourWheels)).shouldBeEqual(1);
        a(Classification.count()).shouldBeEqual(1);
        a(car.getAll(Classification.class).get(0).get("name")).shouldBeEqual("sedan");
    }


    @Test
    public void shouldInferPolymorphicNames() {

        deleteAndPopulateTables("vehicles", "mammals", "classifications");
        Vehicle car = Vehicle.createIt("name", "car");
        car.add(Classification.create("name", "four wheeled"));
        car.add(Classification.create("name", "sedan"));

        List<Classification> classifications = (List<Classification>) car.get("classifications");
        a(classifications).shouldNotBeNull();
        a(classifications.size()).shouldBeEqual(2);
    }

    @Test
    public void shouldFindPolymorphicParent() {

        deleteAndPopulateTables("vehicles", "mammals", "classifications");
        Vehicle.createIt("name", "bike");
        Vehicle  veh = Vehicle.createIt("name", "car");
        veh.add(Classification.create("name", "four wheeled"));
        veh.add(Classification.create("name", "sedan"));

        Vehicle v = Classification.findAll().get(0).parent(Vehicle.class);
        a(v).shouldNotBeNull();
        a(v.get("name")).shouldBeEqual("car");

    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfWrongParentTypeRequested() {

        deleteAndPopulateTables("vehicles", "mammals", "classifications");
        Vehicle car = Vehicle.createIt("name", "car");
        car.add(Classification.create("name", "four wheeled"));
        car.add(Classification.create("name", "sedan"));

        Classification.findAll().get(0).parent(Mammal.class);
    }
}
