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

import org.javalite.activejdbc.associations.NotAssociatedException;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.javalite.common.Convert;
import org.javalite.test.jspec.DifferenceExpectation;
import org.javalite.test.jspec.ExceptionExpectation;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.javalite.activejdbc.test.JdbcProperties.driver;
import static org.javalite.common.Collections.*;


public class ModelTest extends ActiveJDBCTest {

    @Test
    public void testFindOrCreateIt() {
        deleteFromTable("people");
        the(Base.count("people")).shouldBeEqual(0);
        //Create new Person
        Person person1 = Person.findOrCreateIt("name", "yakka", "last_name", "newbie", "dob", getDate(1990, 8, 3));
        the(person1).shouldNotBeNull();
        the(person1.exists()).shouldBeTrue();

        a(Base.count("people", "name = ? and last_name = ?", "yakka", "newbie")).shouldBeEqual(1);

        //Fetch Existing Person
        Person person2 = Person.findOrCreateIt("name", "yakka", "last_name", "newbie", "dob", getDate(1990, 8, 3));
        the(person2).shouldNotBeNull();
        //Verify the id
        the(person1.getId()).shouldBeEqual(person2.getId());
    }

    @Test
    public void testModelFinder() {
        deleteAndPopulateTable("people");
        Person p = new Person();
        p.set("name", "igor", "last_name", "polevoy").saveIt();
        p.refresh();
        List<Person> list = Person.where("name = 'John'").orderBy("dob desc");
        a(1).shouldBeEqual(list.size());
    }

    @Test
    public void testModelFinderWithParams() {

        deleteAndPopulateTable("people");
        List<Person> list = Person.where("name = ?", "John");
        a(1).shouldBeEqual(list.size());
    }

    @Test
    public void testModelFinderWithListener() {
        deleteAndPopulateTable("people");
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

        ModelListener<Person> modelListener = person -> counter ++; // side effect :)
        Person.findWith(modelListener, "name like ?", "%2%");
        a(counter).shouldEqual(19);
    }

    @Test
    public void testModelFindOne() {
        deleteAndPopulateTable("people");
        Person person = Person.findFirst("id = 2");
        a(person).shouldNotBeNull();
    }

    @Test
    public void testModelFindOneParametrized() {
        deleteAndPopulateTable("people");
        Person person = Person.findFirst("id = ?", 2);
        a(person).shouldNotBeNull();
    }

    @Test
    public void testModelFinderAll() {
        deleteAndPopulateTable("people");
        List<Person> list = Person.findAll();
        a(4).shouldBeEqual(list.size());
    }

    @Test
    public void testCreateNewAndSave() {
        deleteAndPopulateTable("people");
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
        deleteAndPopulateTable("people");
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
        deleteAndPopulateTable("people");
        final Person p = new Person();
        expect(new ExceptionExpectation(IllegalArgumentException.class) {
            @Override
            public void exec() {
                p.set("NAME1", "Igor");
            }
        });
    }

    @Test
    public void testAttemptSetId() {
        deleteAndPopulateTable("people");
        final Person p = new Person();

        expect(new ExceptionExpectation(IllegalArgumentException.class) {
            @Override
            public void exec() {
                p.set("person_id", "hehe");
            }
        });
    }

    @Test
    public void testIdName() {
        deleteAndPopulateTables("people", "animals");
        a(new Person().getIdName()).shouldBeEqual("id");
        a(new Animal().getIdName()).shouldBeEqual("animal_id");
    }

    @Test
    public void testLookupAndSave() {
        deleteAndPopulateTable("people");
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
        deleteAndPopulateTable("people");
        Person p = Person.findById(1);
        a(p).shouldNotBeNull();
    }

    @Test
    public void shouldGetAttributeCaseInsensitive() {
        deleteAndPopulateTable("people");
        Person p = Person.findById(1);
        the(p.get("Last_Name")).shouldBeEqual(p.get("last_name"));
        the(p.get("LAST_NAME")).shouldBeEqual(p.get("last_name"));
    }

    @Test
    public void testCount() {
        deleteAndPopulateTable("addresses");
        a(Address.count()).shouldBeEqual(7);
    }

    @Test
    public void testLikeCondition() {
        deleteAndPopulateTable("people");
        a(Person.find("name like ?", "%J%").size()).shouldBeEqual(2);
    }

    @Test
    public void testInstanceDelete() {
        deleteAndPopulateTable("people");
        Person p = Person.findById(1);
        p.delete();

        a(3L).shouldBeEqual(Person.count());
    }

    @Test
    public void testBatchDelete() {
        deleteAndPopulateTable("people");
        Person.delete("name like ?", "%J%");
        a(Person.count()).shouldBeEqual(2L);
    }

    @Test
    public void testBatchUpdate() {
        deleteAndPopulateTable("people");
        Person.update("name = ?, last_name = ?", "name like ?", "blank_name", "blank last name", "%J%");
        a(Person.find("name like ?", "%blank%").size()).shouldBeEqual(2);
    }

    @Test
    public void testBatchUpdateAll() {
        deleteAndPopulateTable("people");
        expect(new DifferenceExpectation(Person.find("last_name like ?", "Smith").size()) {
            @Override
            public Object exec() {
                Person.updateAll("last_name = ?", "Smith");
                return Person.find("last_name like ?", "Smith").size();
            }
        } );
    }

    @Test
    public void testValidatesPresenceOf() {
        deleteAndPopulateTable("people");
        Person p = new Person();
        p.set("name", "");
        p.validate();
        a(p.errors().size()).shouldBeEqual(2);//two validation messages for dob and one for last_name
    }

    @Test
    public void testOverrideTableName() {
        deleteAndPopulateTable("legacy_universities");
        a("legacy_universities").shouldBeEqual(University.getTableName());

        List<University> universities = University.findAll();
        System.out.println(universities);
    }

    @Test
    public void testOneToMany() {
        deleteAndPopulateTables("users", "addresses");
        User user = User.findById(1);
        List<Address> addresses = user.getAll(Address.class);

        a(3).shouldBeEqual(addresses.size());
    }

    @Test
    public void testOneToManyWrongAssociation() {
        deleteAndPopulateTables("users", "addresses");
        final User user = User.findById(1);
        expect(new ExceptionExpectation(NotAssociatedException.class){
            @Override
            public void exec() {
                user.getAll(Book.class);//wrong table
            }
        });

        expect(new ExceptionExpectation(NotAssociatedException.class){
            @Override
            public void exec() {
                user.getAll(Book.class);//non-existent table
            }
        });

    }

    @Test
    public void testBelongsToConvention(){
        deleteAndPopulateTables("users", "addresses");
        a(Address.belongsTo(User.class)).shouldBeTrue();

    }

    @Test
    public void testCustomIdName(){
       deleteAndPopulateTable("animals");
       Animal a = Animal.findById(1);
       a(a).shouldNotBeNull();
    }

    @Test
    public void testOneToManyOverrideConventionAssociation(){
        deleteAndPopulateTables("libraries", "books");
        Library l = Library.findById(1);
        List<Book> books = l.getAll(Book.class);
        Library lib = books.get(0).parent(Library.class);
        the(lib).shouldNotBeNull();
        the(l.getId()).shouldBeEqual(lib.getId());

    }


    @Test
    public void testBelongsToMany(){
        deleteAndPopulateTables("doctors", "patients", "doctors_patients");
        a(Patient.belongsTo(Doctor.class)).shouldBeTrue();
    }

    @Test
    public void testFk(){
        deleteAndPopulateTables("libraries", "books");
        String fk = Library.getMetaModel().getFKName();
        a(fk).shouldBeEqual("library_id");
    }

    @Test
    public void testSaveOneToManyAssociation(){
        deleteAndPopulateTables("users", "addresses");
        User u = User.findById(1);
        Address a = new Address();

        a.set("address1", "436 Barnaby Ct.");
        a.set("address2", "");
        a.set("city", "Wheeling");
        a.set("state", "IL");
        a.set("zip", "60090");
        u.add(a);

        u = User.findById(1);
        System.out.println(u);

        a = new Address();

        a.set("address1", "436 Barnaby Ct.").set("address2", "").set("city", "Wheeling")
                .set("state", "IL").set("zip", "60090");
        u.add(a);
        a(9).shouldBeEqual(Address.count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAddNull(){
        deleteAndPopulateTables("users", "addresses");
        User u = User.findById(1);
        u.add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotRemoveNull(){
        deleteAndPopulateTables("users", "addresses");
        User u = User.findById(1);
        u.remove(null);
    }

    @Test
    public void testCopyTo(){
        deleteAndPopulateTables("users");
        User u = User.findById(1);
        User u1 = new User();
        u.copyTo(u1);
        a(u1.get("first_name")).shouldBeEqual("Marilyn");
        a(u1.getId()).shouldBeNull();
    }

    @Test
    public void testCopyFrom(){
        deleteAndPopulateTables("users");
        User u = User.findById(1);
        User u1 = new User();
        u1.copyFrom(u);
        a(u1.get("first_name")).shouldBeEqual("Marilyn");
        a(u1.getId()).shouldBeNull();
    }

    @Test
    public void testFindBySQL(){
        deleteAndPopulateTables("libraries", "books");
        List<Book> books = Book.findBySQL("select books.*, address from books, libraries where books.lib_id = libraries.id order by address");
        a(books.size()).shouldBeEqual(2);
    }

    @Test
    public void testFrozen(){
        deleteAndPopulateTables("users", "addresses");
        final User u = User.findById(1);
        final Address a = new Address();

        a.set("address1", "436 Flamingo St.");
        a.set("address2", "");
        a.set("city", "Springfield");
        a.set("state", "IL");
        a.set("zip", "60074");
        u.add(a);

        a.delete();

        expect(new ExceptionExpectation(FrozenException.class) {
            @Override
            public void exec() {
                a.saveIt();
            }
        });

        expect(new ExceptionExpectation(FrozenException.class) {
            @Override
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
        deleteAndPopulateTables("users", "addresses");
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

        a(User.findAll().size()).shouldBeEqual(4);
        a(Address.findAll().size()).shouldBeEqual(9);
        u.deleteCascade();

        a(User.findAll().size()).shouldBeEqual(3);
        a(Address.findAll().size()).shouldBeEqual(7);

    }

    @Test
    public void shouldGenerateCorrectInsertSQL() {
        Student s = new Student();
        s.set("first_name", "Jim");
        s.set("last_name", "Cary");
        s.set("id", 1);
        String insertSQL = s.toInsert();
        // date literals formatting is not the same for every DBMS, so not testing this here
        the(insertSQL).shouldBeEqual("INSERT INTO students (first_name, id, last_name) VALUES ('Jim', 1, 'Cary')");

        s.set("dob", getDate(1965, 12, 1));

        insertSQL = s.toInsert();
        the(insertSQL).shouldBeEqual("INSERT INTO students (dob, first_name, id, last_name) VALUES (DATE '1965-12-01', 'Jim', 1, 'Cary')");
    }

    @Test
    public void shouldGenerateCorrectInsertSQLWithReplacements() {
        Student s = new Student();
        s.set("first_name", "Jim");
        s.set("last_name", "O'Connor's");

        String insertSQL = s.toInsert("'", "''");

        the(insertSQL).shouldBeEqual("INSERT INTO students (first_name, last_name) VALUES ('Jim', 'O''Connor''s')");

        s.set("dob", getDate(1965, 12, 1));

        insertSQL = s.toInsert("'", "''");

        the(insertSQL).shouldBeEqual("INSERT INTO students (dob, first_name, last_name) VALUES (DATE '1965-12-01', 'Jim', 'O''Connor''s')");

        the(Base.exec(insertSQL)).shouldBeEqual(1);

        Student insertedStudent = Student.findFirst("last_name = ?", "O'Connor's");
        the(insertedStudent).shouldNotBeNull();
        the(insertedStudent.get("last_name")).shouldBeEqual("O'Connor's");
    }

    @Test
    public void shouldGenerateValidUpdateSQL() {
        deleteAndPopulateTable("students");
        Student s = Student.findById(1);
        s.set("first_name", "James", "last_name", "Meredith");
        java.sql.Date dob = getDate(1933, 6, 25);
        java.sql.Timestamp enrollmentDate = getTimestamp(1962, 10, 1, 12, 0, 0, 0);
        s.setDate("dob", dob);
        s.setTimestamp("enrollment_date", enrollmentDate);
        // don't save it!

        String updateSql = s.toUpdate();
        the(Base.exec(updateSql)).shouldBeEqual(1);

        s = Student.findById(1);
        the(s.get("first_name")).shouldBeEqual("James");
        the(s.get("last_name")).shouldBeEqual("Meredith");
        the(s.get("dob")).shouldBeEqual(dob);
        the(s.get("enrollment_date")).shouldBeEqual(enrollmentDate);
    }

    @Test
    public void shouldGenerateValidUpdateSQLWithReplacements() {
        deleteAndPopulateTable("students");
        Student s = Student.findById(1);
        s.set("first_name", "James", "last_name", "O'Connor's");

        String updateSql = s.toUpdate("'", "''"); // replace single quotes with two single quotes
        the(Base.exec(updateSql)).shouldBeEqual(1);

        s = Student.findById(1);
        the(s.get("first_name")).shouldBeEqual("James");
        the(s.get("last_name")).shouldBeEqual("O'Connor's");
    }

    @Test
    public void shouldGenerateValidUpdateSQLWithTime() {
        Alarm alarm = new Alarm();
        alarm.setTime("alarm_time", java.sql.Time.valueOf("12:34:56"));
        alarm.save();

        String t = "01:23:45";
        alarm.setTime("alarm_time", java.sql.Time.valueOf(t));
        String updateSql = alarm.toUpdate();
        the(Base.exec(updateSql)).shouldBeEqual(1);

        alarm = Alarm.findById(alarm.getId());

        if(driver().contains("jtds")){
            the(alarm.getString("alarm_time").startsWith(t)).shouldBeTrue();
        }else {
            the(alarm.getTime("alarm_time").toString()).shouldBeEqual(t);
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldGenerateNoSuchElementFromBlankUpdate() {
    	// Verify that a model with no attributes throws an error
    	Student s = new Student();
    	s.toUpdate();
    }

    @Test
    public void shouldGenerateValidInsertSQL() {
        Student s = new Student();
        s.set("first_name", "Jim", "last_name", "Cary");
        java.sql.Date dob = getDate(1965, 12, 1);
        java.sql.Timestamp enrollmentDate = getTimestamp(1973, 1, 20, 11, 0, 0, 0);
        s.setDate("dob", dob);
        s.setTimestamp("enrollment_date", enrollmentDate);

        String insertSql = s.toInsert();
        Object id = Base.execInsert(insertSql, s.getIdName());

        s = Student.findById(id);
        the(s.get("first_name")).shouldBeEqual("Jim");
        the(s.get("last_name")).shouldBeEqual("Cary");
        the(s.get("dob")).shouldBeEqual(dob);
        the(s.get("enrollment_date")).shouldBeEqual(enrollmentDate);
    }

    @Test
    public void shouldGenerateValidInsertSQLWithTime() {
        Alarm alarm = new Alarm();
        String t = "12:34:56";
        alarm.setTime("alarm_time", java.sql.Time.valueOf(t));

        String insertSql = alarm.toInsert();
        Object id = Base.execInsert(insertSql, alarm.getIdName());

        alarm = Alarm.findById(id);

        if(driver().contains("jtds")){
            the(alarm.getString("alarm_time").startsWith(t)).shouldBeTrue();
        }else {
            the(alarm.getTime("alarm_time").toString()).shouldBeEqual(t);
        }
    }

    @Test
    public void shouldFindManyToOneViaGetter() {
        deleteAndPopulateTables("users", "addresses");
        Address address = Address.findById(1);
        User u = (User)address.get("user");
        a(u).shouldNotBeNull();
    }

    @Test
    public void shouldFindOneToManyViaGetter() {
        deleteAndPopulateTables("users", "addresses");
        User user = User.findById(1);
        List<Address> addresses = (List<Address>)user.get("addresses");
        a(3).shouldBeEqual(addresses.size());
    }

    @Test
    public void shouldCreateModelWithSingleSetter(){
        deleteAndPopulateTable("people");
        expect(new DifferenceExpectation(Person.count()) {
            @Override
            public Object exec() {
                new Person().set("name", "Marilyn", "last_name", "Monroe", "dob", "1935-12-06").saveIt();
                return (Person.count());
            }
        });
    }

    @Test
    public void shouldCollectLastNames() {
        deleteAndPopulateTable("people");
        List<String> lastNames = Person.findAll().orderBy("name").collect("last_name");
        the(lastNames).shouldBeEqual(list("Pesci", "Smith", "Jonston", "Ali"));
    }

    @Test
    public void shouldCollectDistictFirstNames() {
        deleteAndPopulateTable("patients");
        Set<String> firstNames = Patient.findAll().collectDistinct("first_name");
        the(firstNames.size()).shouldBeEqual(2);
        the(firstNames.contains("Jim")).shouldBeTrue();
        the(firstNames.contains("John")).shouldBeTrue();
    }

    @Test
    public void shouldFindChildrenWithCriteria(){
        deleteAndPopulateTables("users", "addresses");
        User user = User.findById(1);

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
        p.set("dob", "2014-11-07");
        p.saveIt();
        a(p.get("name")).shouldBeEqual("John");
        a(p.get("last_name")).shouldBeEqual("Deer");
        a(p.get("dob")).shouldNotBeNull();
        Object id  = p.getId();

        p.fromMap(map("name", "Jack", "dob", null));

        a(p.get("name")).shouldBeEqual("Jack");
        a(p.get("last_name")).shouldBeEqual("Deer");
        a(p.get("dob")).shouldBeNull();
        a(p.getId()).shouldBeEqual(id);
    }


    @Test
    public void shouldSerializeModel() throws IOException, ClassNotFoundException {
        deleteAndPopulateTable("people");
        Person p = Person.findById(1);

        //write model
        ByteArrayOutputStream bout =  new ByteArrayOutputStream();
        ObjectOutputStream  out = new ObjectOutputStream(bout);
        out.writeObject(p);
        out.flush();

        //read model
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
        Person p1 = (Person) in.readObject();

        //validate models
        a(p1.get("name")).shouldBeEqual(p.get("name"));
        a(p1.get("last_name")).shouldBeEqual(p.get("last_name"));
        a(p1.get("dob")).shouldBeEqual(p.get("dob"));
    }

    @Test
    public void shouldSerializeModelWithDirtyAttributes() throws IOException, ClassNotFoundException {
        deleteAndPopulateTable("people");
        Person p = Person.findById(1);


        p.set("name", "Jeff");

        //write model
        ByteArrayOutputStream bout =  new ByteArrayOutputStream();
        ObjectOutputStream  out = new ObjectOutputStream(bout);
        out.writeObject(p);
        out.flush();

        //read model
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
        Person p1 = (Person) in.readObject();

        //validate models
        a(p1.get("name")).shouldBeEqual(p.get("name"));
        a(p1.get("last_name")).shouldBeEqual(p.get("last_name"));
        a(p1.get("dob")).shouldBeEqual(p.get("dob"));
        a(p1.isModified()).shouldBeTrue();
    }


    @Test
    public void shouldConvertUtilDate2SqlDate() {
        Person p = new Person();
        p.setDate("dob", Convert.truncateToSqlDate(new java.util.Date()));
        a(p.get("dob")).shouldBeA(java.sql.Date.class);

        java.sql.Date date = p.getDate("dob");
        Calendar c = new GregorianCalendar();
        c.setTime(date);

        a(date.toString()).shouldBeEqual(new java.sql.Date(System.currentTimeMillis()).toString());
        a(c.get(Calendar.HOUR_OF_DAY)).shouldBeEqual(0);
        a(c.get(Calendar.MINUTE)).shouldBeEqual(0);
        a(c.get(Calendar.SECOND)).shouldBeEqual(0);
        a(c.get(Calendar.MILLISECOND)).shouldBeEqual(0);
    }

    @Test
    public void testNewFromMap() {
        Person p = new Person().fromMap(map("id", null, "name", "Joe", "last_name", "Schmoe"));

        a(p.getId()).shouldBeNull();
        a(p.isNew()).shouldBeTrue();
        a(p.isValid()).shouldBeTrue();
    }

    @Test
    public void testNewFromMapCaseInsensitive() {
        Person p = new Person().fromMap(map("NAME", "Joe", "Last_Name", "Schmoe", "dob", "2003-06-15"));

        a(p.get("name")).shouldNotBeNull();
        a(p.get("last_name")).shouldNotBeNull();
        a(p.get("dob")).shouldNotBeNull();
    }

    @Test
    public void shouldConvertTime() {
        String t = "10:30:00";
        Alarm alarm = new Alarm().setTime("alarm_time", t);
        the(alarm.get("alarm_time")).shouldBeA(java.sql.Time.class);
        alarm.save();

        alarm = Alarm.findById(alarm.getId());

        if(driver().contains("jtds")){
            the(alarm.getString("alarm_time").startsWith(t)).shouldBeTrue();
        }else {
            the(alarm.getTime("alarm_time").toString()).shouldBeEqual(t);
        }
    }
}
