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
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class IncludesTest extends ActiveJDBCTest{

    @Test
    public void shouldBeAbleToIncludeParentOne2Many() {
        deleteAndPopulateTables("users", "addresses");
        List<Address> addresses = Address.where("city = ?", "Springfield").orderBy("id").include(User.class);
        //ensure that the parent is actually cached
        User u1 = addresses.get(0).parent(User.class);
        User u2 = addresses.get(0).parent(User.class);
        a(u1).shouldBeTheSameAs(u2);

        a(addresses.get(0).get("user")).shouldNotBeNull();
        User user = (User) addresses.get(0).get("user");
        a(user.get("first_name")).shouldBeEqual("Marilyn");

        user = (User)addresses.get(6).get("user");
        a(user.get("first_name")).shouldBeEqual("John");
    }

    @Test
    public void shouldBeAbleToIncludeChildrenOne2Many() {
        deleteAndPopulateTables("users", "addresses");
        LazyList<User> users = User.findAll().orderBy("id").include(Address.class);
        List<Map<String, Object>> maps = users.toMaps();

        Map<String, Object> user = maps.get(0);
        a(user.get("first_name")).shouldBeEqual("Marilyn");
        List<Map> addresses = (List<Map>)user.get("addresses");
        a(addresses.size()).shouldBeEqual(3);

        a(addresses.get(0).get("address1")).shouldBeEqual("123 Pine St.");
        a(addresses.get(1).get("address1")).shouldBeEqual("456 Brook St.");
        a(addresses.get(2).get("address1")).shouldBeEqual("23 Grove St.");
    }

    @Test
    public void shouldFixDefect553CacheEmptyChildren()
    {
        deleteAndPopulateTables("users", "addresses");
        LazyList<User> users = User.findAll().orderBy("id").include(Address.class);

        LazyList<Address> addresses1 = users.get(2).getAll(Address.class);
        LazyList<Address> addresses2 = users.get(2).getAll(Address.class);

        a(addresses1).shouldBeTheSameAs(addresses2);
        a(addresses1.isEmpty()).shouldBeTrue();
    }

    @Test
    public void shouldBeAbleToIncludeOtherInManyToMany() {
        deleteAndPopulateTables("doctors", "patients", "doctors_patients");
        LazyList<Doctor> doctors = Doctor.findAll().orderBy("id").include(Patient.class);

        Doctor doctor0 = doctors.get(0);
        List<Patient> patientList = doctor0.getAll(Patient.class);
        List<Patient> patientList1 = doctor0.getAll(Patient.class);

        the(patientList.size()).shouldBeEqual(2);

        //ensure cached:
        the(patientList).shouldBeTheSameAs(patientList1);
        the(patientList.get(0)).shouldBeTheSameAs(patientList1.get(0));

        List<Map<String, Object>> doctorsMaps = doctors.toMaps();

        List<Map> patients = (List<Map>)doctorsMaps.get(0).get("patients");
        a(patients.size()).shouldBeEqual(2);

        patients = (List<Map>)doctorsMaps.get(1).get("patients");
        a(patients.size()).shouldBeEqual(1);
    }


    @Test
    public void shouldNotCallDependentIfOriginatedModelQueryReturnsNoResultsForManyToMany() {
        deleteAndPopulateTables("doctors", "patients", "doctors_patients");

        LazyList<Patient> patients = Doctor.find("last_name = ?", " does not exist").include(Patient.class);

        a(patients.size()).shouldEqual(0);
    }

    @Test
    public void shouldNotCallDependentIfOriginatedModelQueryReturnsNoResultsForOneToMany() {
        deleteAndPopulateTables("users", "addresses");

        LazyList<Address> addresses = User.find("email = ?", " does not exist").include(Address.class);

        a(addresses.size()).shouldEqual(0);
    }



    @Test
    public void shouldCacheIncludesMany2Many() {
        deleteAndPopulateTables("doctors", "patients", "doctors_patients");
        LazyList<Doctor> doctors = Doctor.findAll().orderBy("id").include(Patient.class);

        List<Patient> patients1 = doctors.get(0).getAll(Patient.class);
        List<Patient> patients2 = doctors.get(0).getAll(Patient.class);
        a(patients1).shouldBeTheSameAs(patients2);
    }

    @Test
    public void shouldFixDefect553CacheIncludesMany2Many() {
        deleteAndPopulateTables("doctors", "patients", "doctors_patients");
        LazyList<Doctor> doctors = Doctor.findAll().orderBy("id").include(Patient.class);

        // Doctor without patients
        List<Patient> patients1 = doctors.get(3).getAll(Patient.class);
        List<Patient> patients2 = doctors.get(3).getAll(Patient.class);
        a(patients1).shouldBeTheSameAs(patients2);
        a(patients1.isEmpty()).shouldBeTrue();
    }

    @Test
    public void shouldBeAbleToIncludeParentAndChildren() {
        deleteAndPopulateTables("libraries", "books", "readers");
        List<Book> books = Book.findAll().orderBy(Book.getMetaModel().getIdName()).include(Reader.class, Library.class);
        Map book = books.get(0).toMap();

        List<Map> readers = (List<Map>)book.get("readers");
        a(readers.get(0).get("last_name")).shouldBeEqual("Smith");
        a(readers.get(1).get("last_name")).shouldBeEqual("Doe");

        Map library = (Map)book.get("library");
        a(library.get("address")).shouldBeEqual("124 Pine Street");
    }

    @Test
    public void shouldIncludeMany2ManyInCaseJoinTableHasUnconventionalPKName() {
        Ingredient sugar = Ingredient.createIt("ingredient_name", "sugar");
        sugar.add(Recipe.create("recipe_name", "pie"));

        //test data:
        List<Ingredient> ingredients  = Ingredient.findAll().include(Recipe.class);
        List<Recipe> recipes = ingredients.get(0).getAll(Recipe.class);
        a(recipes.size()).shouldBeEqual(1);
        a(recipes.get(0).get("recipe_name")).shouldBeEqual("pie");


        //test caching (no more trips to DB):
        Recipe recipe1 = ingredients.get(0).getAll(Recipe.class).get(0);
        Recipe recipe2 = ingredients.get(0).getAll(Recipe.class).get(0);
        a(recipe1).shouldBeTheSameAs(recipe2);
    }

    @Test
    public void shouldFixDefect163NeedsToIncludeChildrenAndParentsInTreeStructure(){
        Node car = new Node("Car");
        car.saveIt();

        Node sedan = new Node("Sedan");
        car.add(sedan);

        Node sportsSedan = new Node("Sports sedan");
        sedan.add(sportsSedan);

        Node sedan1 = (Node) Node.find("name = ?", "Sedan").include(Node.class).get(0);

        Node parent1 = sedan1.parent(Node.class);
        Node parent2 = sedan1.parent(Node.class);

        //should return cached parent in both cases
        a(parent1).shouldBeTheSameAs(parent2);

        the(parent1.get("name")).shouldBeEqual("Car");

        List<Node> children = sedan1.getAll(Node.class);
        the(children.size()).shouldBeEqual(1);
        the(children.get(0).get("name")).shouldBeEqual("Sports sedan");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectClassWithNoAssociation(){
        Ingredient.findAll().include(Article.class);
    }


    @Test
    public void shouldNotIncludeParentIfParentIdIsNull() {
        deleteAndPopulateTable("nodes");

        Node node = (Node) Node.find("name = ?", "Parent").include(Node.class).get(0);
        // see log
    }

    @Test
    public void shouldIncludeParentAndChildren() {
        deleteAndPopulateTable("nodes");

        Node node = (Node) Node.find("name = ?", "Self").include(Node.class).get(0);
        // see log
    }

}
