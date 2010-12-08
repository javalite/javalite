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


package activejdbc;

import activejdbc.associations.NotAssociatedException;
import activejdbc.test.ActiveJDBCTest;
import javalite.test.jspec.DifferenceExpectation;
import javalite.test.jspec.ExceptionExpectation;
import activejdbc.test_models.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static javalite.common.Collections.map;


public class ModelTest extends ActiveJDBCTest {

    @Test
    public void testModelFinder() {
        resetTable("people");

        List<Person> list = Person.where("name = 'John'").orderBy("dob desc");
        a(1).shouldBeEqual(list.size());
    }

    @Test
    public void testModelFinderWithParams() {

        resetTable("people");
        List<Person> list = Person.where("name = ?", "John");
        a(1).shouldBeEqual(list.size());
    }

    @Test
    public void testModelFinderWithListener() {
        resetTable("people");
        Person.findWith(new ModelListener<Person>() {
            public void onModel(Person person) {
                System.out.println("Found person: " + person);
            }
        }, "name='John'");
    }


    int counter = 0;

    @Test
    public void testModelFinderWithListenerAndParameters() {

        Person.deleteAll();
        for(int i = 0; i < 100; i++){
            Person.createIt("name", "Name: " + i, "last_name", "Last Name: " + i);
        }

        ModelListener<Person> modelListener = new ModelListener<Person>() {
            public void onModel(Person person) {
                counter ++;
            }
        };
        Person.findWith(modelListener, "name like ?", "%2%");
        a(counter).shouldEqual(19);
    }

    @Test
    public void testModelFindOne() {
        resetTable("people");
        Person person = (Person)Person.findFirst("id = 2");
        a(person).shouldNotBeNull();
    }

    @Test
    public void testModelFindOneParametrized() {
        resetTable("people");
        Person person = (Person)Person.findFirst("id = ?", 2);
        a(person).shouldNotBeNull();
    }

    @Test
    public void testModelFinderAll() {
        resetTable("people");
        List<Person> list = Person.findAll();
        a(4).shouldBeEqual(list.size());
    }

    @Test
    public void testCreateNewAndSave() {
        resetTable("people");
        Person p = new Person();
        p.set("name", "Marilyn");
        p.set("last_name", "Monroe");
        p.set("dob", getDate(1935, 12, 6));
        p.saveIt();
        
        a(p.getId()).shouldNotBeNull();
        //verify save:

        List<Map> results = Base.findAll("select * from people where name = ? and last_name = ? and dob = ?", "Marilyn", "Monroe", getDate(1935, 12, 6));

        a(results.size()).shouldBeEqual(1);
    }

    @Test
    public void testCreateNewAndSaveWithSomeNULLs() {
        resetTable("people");
        Person p = new Person();
        p.set("name", "Keith");
        p.set("last_name", "Emerson");
        //DOB setter missing
        p.saveIt();

        //verify save:
        List<Map> results = Base.findAll("select * from people where name = ? and last_name = ?", "Keith", "Emerson");

        a(results.size()).shouldBeEqual(1);
    }

    @Test
    public void testSetWrongAttribute() {
        resetTable("people");
        final Person p = new Person();
        expect(new ExceptionExpectation(IllegalArgumentException.class) {
            public void exec() {
                p.set("NAME1", "Igor");
            }
        });
    }

    @Test
    public void testAttemptSetId() {
        resetTable("people");
        final Person p = new Person();

        expect(new ExceptionExpectation(IllegalArgumentException.class) {
            public void exec() {
                p.set("person_id", "hehe");
            }
        });
    }

    @Test
    public void testIdName() {
        resetTables("people", "animals");
        a(new Person().getIdName()).shouldBeEqual("id");
        a(new Animal().getIdName()).shouldBeEqual("animal_id");
    }

    @Test
    public void testLookupAndSave() {
        resetTable("people");
        List<Person> list = Person.find("id = 1");
        Person p = list.get(0);
        p.set("name", "Igor");
        p.saveIt();

        //verify save:
        List<Map> results = Base.findAll("select * from people where id = 1");

        a(1).shouldBeEqual(results.size());
        a(results.get(0).get("name")).shouldBeEqual("Igor");
    }

    @Test
    public void testGetById() {
        resetTable("people");
        Person p = (Person)Person.findById(1);
        a(p).shouldNotBeNull();
    }

    @Test
    public void testCount() {
        resetTable("people");
        a(Person.count()).shouldBeEqual(4L);
    }

    @Test
    public void testLikeCondition() {
        resetTable("people");
        a(Person.find("name like ?", "%J%").size()).shouldBeEqual(2);
    }

    @Test
    public void testInstanceDelete() {
        resetTable("people");
        Person p = (Person)Person.findById(1);
        p.delete();

        a(3L).shouldBeEqual(Person.count());
    }

    @Test
    public void testBatchDelete() {
        resetTable("people");
        Person.delete("name like ?", "%J%");
        a(Person.count()).shouldBeEqual(2L);
    }

    @Test
    public void testBatchUpdate() {
        resetTable("people");
        Person.update("name = ?, last_name = ?", "name like ?", "blank_name", "blank last name", "%J%");
        a(Person.find("name like ?", "%blank%").size()).shouldBeEqual(2);
    }

    @Test
    public void testBatchUpdateAll() {
        resetTable("people");
        expect(new DifferenceExpectation(Person.find("last_name like ?", "Smith").size()) {
            public Object exec() {
                Person.updateAll("last_name = ?", "Smith");
                return Person.find("last_name like ?", "Smith").size();
            }
        } );
    }

    @Test
    public void testValidatesPresenceOf() {
        resetTable("people");
        Person p = new Person();
        p.set("name", "");
        p.validate();
        a(p.errors().size()).shouldBeEqual(2);//two validation messages for dob and one for last_name
    }

    @Test
    public void testOverrideTableName() {
        resetTable("legacy_universities");
        a("legacy_universities").shouldBeEqual(University.getTableName());

        List<University> universities = University.findAll();
        System.out.println(universities);
    }

    @Test
    public void testOneToMany() {
        resetTables("users", "addresses");
        User user = (User)User.findById(1);
        List<Address> addresses = user.getAll(Address.class);

        a(3).shouldBeEqual(addresses.size());
    }

    @Test
    public void testOneToManyWrongAssociation() {
        resetTables("users", "addresses");
        final User user = (User)User.findById(1);
        expect(new ExceptionExpectation(NotAssociatedException.class){
            public void exec() {
                user.getAll(Book.class);//wrong table
            }
        });

        expect(new ExceptionExpectation(NotAssociatedException.class){
            public void exec() {
                user.getAll(Book.class);//non-existent table
            }
        });
        
    }

    @Test
    public void testBelongsToConvention(){
        resetTables("users", "addresses");
        a(Address.belongsTo(User.class)).shouldBeTrue();

    }

    @Test
    public void testCustomIdName(){
        resetTables("animals");
       Animal a = (Animal)Animal.findById(1);
       a(a).shouldNotBeNull();
    }

    @Test
    public void testOneToManyOverrideConventionAssociation(){
        resetTables("libraries", "books");
        Library l = (Library)Library.findById(1);
        List<Book> books = l.getAll(Book.class);
        Library lib = (Library)books.get(0).parent(Library.class);
        the(lib).shouldNotBeNull();
        the(l.getId()).shouldBeEqual(lib.getId());

    }


    @Test
    public void testBelongsToMany(){
        resetTables("doctors", "patients", "doctors_patients");
        a(Patient.belongsTo(Doctor.class)).shouldBeTrue();
    }

    @Test
    public void testFk(){
        resetTables("libraries", "books");
        String fk = Library.getMetaModel().getFKName();
        a(fk).shouldBeEqual("library_id");
    }

    @Test
    public void testSaveOneToManyAssociation(){
        resetTables("users", "addresses");
        User u = (User)User.findById(1);
        Address a = new Address();

        a.set("address1", "436 Barnaby Ct.");
        a.set("address2", "");
        a.set("city", "Wheeling");
        a.set("state", "IL");
        a.set("zip", "60090");
        u.add(a);

        u = (User)User.findById(1);
        System.out.println(u);

        a = new Address();

        a.set("address1", "436 Barnaby Ct.").set("address2", "").set("city", "Wheeling")
                .set("state", "IL").set("zip", "60090");
        u.add(a);
        a(9).shouldBeEqual(Address.count());

    }

    @Test
    public void testCopyTo(){
        resetTables("users", "addresses");
        User u = (User)User.findById(1);
        User u1 = new User();
        u.copyTo(u1);
        a(u1.get("first_name")).shouldBeEqual("Marilyn");
    }

    @Test
    public void testCopyFrom(){
        resetTables("users", "addresses");
        User u = (User)User.findById(1);
        User u1 = new User();
        u1.copyFrom(u);
        a(u1.get("first_name")).shouldBeEqual("Marilyn");
    }

    @Test
    public void testFindBySQL(){
        resetTables("libraries", "books");
        List<Book> books = Book.findBySQL("select books.*, address from books, libraries where books.lib_id = libraries.id order by address");
        a(books.size()).shouldBeEqual(2);
    }

    @Test
    public void testFrosen(){
        resetTables("users", "addresses");
        final User u = (User)User.findById(1);
        final Address a = new Address();

        a.set("address1", "436 Flamingo St.");
        a.set("address2", "");
        a.set("city", "Springfield");
        a.set("state", "IL");
        a.set("zip", "60074");
        u.add(a);

        a.delete();

        expect(new ExceptionExpectation(FrozenException.class) {
            public void exec() {
                a.saveIt();
            }
        });

        expect(new ExceptionExpectation(FrozenException.class) {
            public void exec() {
                u.add(a);
            }
        });

        a.thaw();

        expect(new DifferenceExpectation(u.getAll(Address.class).size()) {
            @Override
            public Object exec() {
                u.add(a);
                return u.getAll(Address.class).size();
            }
        });
        u.add(a);
    }

    @Test
    public void testDeleteCascade(){
        resetTables("users", "addresses");
        final User u = new User();
        u.set("first_name", "Homer");
        u.set("last_name", "Simpson");
        u.set("email", "homer@nukelarplant.com");
        u.saveIt();

        Address a = new Address();
        a.set("address1", "436 Flamingo St.");
        a.set("address2", "");
        a.set("city", "Springfield");
        a.set("state", "IL");
        a.set("zip", "60074");
        u.add(a);

        a = new Address();
        a.set("address1", "123 Monty Burns Drive.");
        a.set("address2", "");
        a.set("city", "Springfield");
        a.set("state", "IL");
        a.set("zip", "60074");
        u.add(a);

        a(User.findAll().size()).shouldBeEqual(3);
        a(Address.findAll().size()).shouldBeEqual(9);
        u.deleteCascade();

        a(User.findAll().size()).shouldBeEqual(2);
        a(Address.findAll().size()).shouldBeEqual(7);

    }

    @Test
    @Ignore
    public void testOrdeBy(){
    }

    @Test
    @Ignore
    public void testGroupBy(){
    }

    @Test
    public void shouldGenerateCorrectInsertSQL(){
        resetTables("students", "courses", "registrations");
        Student s = (Student)Student.findById(1);
        String insertSQL = s.toInsert();
        System.out.println(insertSQL);

        the(insertSQL).shouldBeEqual("INSERT INTO students (dob, first_name, id, last_name) VALUES ('1965-12-01', 'Jim', 1, 'Cary')");

        insertSQL = s.toInsert("q'{", "}'");

        the(insertSQL).shouldBeEqual("INSERT INTO students (dob, first_name, id, last_name) VALUES ('1965-12-01', q'{Jim}', 1, q'{Cary}')");

        insertSQL = s.toInsert(new SimpleFormatter(java.sql.Date.class, "to_date('", "')"));
        the(insertSQL).shouldBeEqual("INSERT INTO students (dob, first_name, id, last_name) VALUES (to_date('1965-12-01'), 'Jim', 1, 'Cary')");
    }

    @Test
    public void shouldFindManyToOneViaGetter() {
        resetTables("users", "addresses");
        Address address = Address.<Address>findById(1);
        User u = (User)address.get("user");
        a(u).shouldNotBeNull();
    }

    @Test
    public void shouldFindOneToManyViaGetter() {
        resetTables("users", "addresses");
        User user = (User)User.findById(1);
        List<Address> addresses = (List<Address>)user.get("addresses");
        a(3).shouldBeEqual(addresses.size());
    }

    @Test
    public void shouldCreateModelWithSingleSetter(){
        resetTables("people");
        expect(new DifferenceExpectation(Person.count()) {
            public Object exec() {
                new Person().set("name", "Marilyn", "last_name", "Monroe", "dob", "1935-12-06").saveIt();
                return (Person.count()); 
            }
        });
    }

    @Test
    public void shouldCollectLastNames(){
        resetTables("people");
        List expected= javalite.common.Collections.li("Pesci", "Smith", "Jonston", "Ali");
        a(Person.findAll().orderBy("name").collect("last_name")).shouldBeEqual(expected);
    }

    @Test
    public void shouldFindChildrenWithCriteria(){
        resetTables("users", "addresses");
        User user = (User)User.findById(1);

        a(user.get(Address.class, "address1 = ? or address2 = ?", "456 Brook St.", "apt 21").size()).shouldEqual(1);
    }

    @Test
    public void shouldAcceptUpperCaseAttributeName(){

        Person.deleteAll();
        
        Person p = new Person();
        p.set("NAME", "John");//before the upper case caused exception
        p.set("last_name", "Deer");
        p.saveIt();
        a(Person.count()).shouldBeEqual(1);
    }


    @Test
    public void shouldOverrideSomeAttributesFromMap(){

        Person.deleteAll();

        Person p = new Person();
        p.set("name", "John");//before the upper case caused exception
        p.set("last_name", "Deer");
        p.saveIt();
        Object id  = p.getId();
        System.out.println(p);

        p.fromMap(map("name", "Jack"));
        System.out.println(p);
        a(p.get("name")).shouldBeEqual("Jack");
        a(p.get("last_name")).shouldBeEqual("Deer");
        a(p.getId()).shouldBeEqual(id);
    }

}


