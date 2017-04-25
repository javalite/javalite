package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.javalite.test.jspec.JSpec.$;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 12/24/15.
 */
public class ShardingSpec extends ActiveJDBCTest {

    @After
    public void cleanup(){
        Temperature.metaModel().clearShardTableName();
        Address.metaModel().clearShardTableName();
        User.metaModel().clearShardTableName();
        Address.metaModel().clearShardTableName();
        Doctor.metaModel().clearShardTableName();
        Patient.metaModel().clearShardTableName();
        Article.metaModel().clearShardTableName();
        Post.metaModel().clearShardTableName();
        Comment.metaModel().clearShardTableName();
    }

    /// STANDALONE:
    @Test
    public void shouldAccessDifferentTableFromSameModel() {
        setupStandalone();

        List<Temperature> temperatures = Temperature.findAll().orderBy("id");
        a(temperatures.size()).shouldBeEqual(2);
        a(temperatures.get(0).get("temp")).shouldBeEqual(10);
        a(temperatures.get(1).get("temp")).shouldBeEqual(20);

        //shard 1:
        temperatures = Temperature.findAll().orderBy("id");
        Temperature.metaModel().setShardTableName("shard1_temperatures");
        a(temperatures.size()).shouldBeEqual(3);
        a(temperatures.get(0).get("temp")).shouldBeEqual(30);
        a(temperatures.get(1).get("temp")).shouldBeEqual(40);
        a(temperatures.get(2).get("temp")).shouldBeEqual(50);

        //shard 2:
        Temperature.metaModel().setShardTableName("shard2_temperatures");
        temperatures = Temperature.findAll().orderBy("id");
        a(temperatures.size()).shouldBeEqual(4);
        a(temperatures.get(0).get("temp")).shouldBeEqual(60);
        a(temperatures.get(1).get("temp")).shouldBeEqual(70);
        a(temperatures.get(2).get("temp")).shouldBeEqual(80);
        a(temperatures.get(3).get("temp")).shouldBeEqual(90);
    }

    @Test
    public void shouldInsertIntoShard() {
        setupStandalone();
        Temperature.metaModel().setShardTableName("shard1_temperatures");
        Temperature t = new Temperature();
        t.set("temp", 44).saveIt();
        Object temp = Base.firstCell("select temp from shard1_temperatures order by id desc");
        the(temp).shouldBeEqual(44);
    }

    @Test
    public void shouldDeleteFromShard() {
        setupStandalone();
        Temperature.metaModel().setShardTableName("shard2_temperatures");
        Temperature t = Temperature.findFirst("temp = 60");
        t.delete();
        the(Base.count("shard2_temperatures")).shouldBeEqual(3);
    }


    @Test
    public void shouldUpdateFromShard() {
        setupStandalone();
        Temperature.metaModel().setShardTableName("shard2_temperatures");
        Temperature t = Temperature.findFirst("temp = ?", 60);
        t.set("temp", 91).saveIt();
        the(Base.firstCell("select temp from shard2_temperatures where id = ?", t.getId())).shouldBeEqual(91);
    }


    //// ONE TO MANY:
    @Test
    public void should_access_shard_child_in_one_to_many() {
        deleteAndPopulateTables("users", "addresses");

        Address.metaModel().setShardTableName("shard1_addresses");

        User user = User.findById(1);
        List<Address> addresses = user.getAll(Address.class);
        a(addresses.size()).shouldBeEqual(0);

        a(Base.count("shard1_addresses")).shouldBeEqual(0);

        Address address = new Address();
        address.set("address1", "3344 Chicago Ave.","address2", "", "city", "Wheeling", "state", "IL", "zip", "60090");
        user.add(address);
        address = new Address();
        address.set("address1", "4455 Pine Ct.","address2", "", "city", "Chicago", "state", "IL", "zip", "60606");
        user.add(address);

        //verify directly in the shard:
        a(Base.count("shard1_addresses")).shouldBeEqual(2);
        //verify using API:
        addresses = user.getAll(Address.class).orderBy("id");
        the(addresses.get(0).get("address1")).shouldBeEqual("3344 Chicago Ave.");
        the(addresses.get(1).get("address1")).shouldBeEqual("4455 Pine Ct.");
    }

    @Test
    public void should_access_shard_parent_in_one_to_many() {

        User.metaModel().setShardTableName("shard1_users");

        User user = User.createIt("first_name", "John", "last_name", "Doe", "email", "john@doe.com");

        List<Address> addresses = user.getAll(Address.class);
        a(addresses.size()).shouldBeEqual(0);
        a(Base.count("addresses")).shouldBeEqual(0);

        Address address = new Address();
        address.set("address1", "3344 Chicago Ave.","address2", "", "city", "Wheeling", "state", "IL", "zip", "60090");
        user.add(address);
        address = new Address();
        address.set("address1", "4455 Pine Ct.","address2", "", "city", "Chicago", "state", "IL", "zip", "60606");
        user.add(address);

        //verify directly in  main table:
        a(Base.count("addresses")).shouldBeEqual(2);
        //verify using API:
        addresses = user.getAll(Address.class).orderBy("id");
        the(addresses.get(0).get("address1")).shouldBeEqual("3344 Chicago Ave.");
        the(addresses.get(1).get("address1")).shouldBeEqual("4455 Pine Ct.");

        // now need to verify that our user is from the shard table:
        the(Base.count("shard1_users")).shouldBeEqual(1);

        the(Base.firstCell("select first_name from shard1_users")).shouldBeEqual("John");
        the(Base.firstCell("select last_name from shard1_users")).shouldBeEqual("Doe");
    }


    //// MANY TO MANY:
    @Test
    public void should_access_shard_in_many_to_many() {

        clearMany2Many();

        Doctor.metaModel().setShardTableName("shard1_doctors");
        Doctor d1 = Doctor.createIt("first_name", "John", "last_name", "Doe", "discipline", "surgeon");
        Doctor d2 = Doctor.createIt("first_name", "Mike", "last_name", "Hall", "discipline", "psychiatrist");

        //patients created in memory:
        Patient p1 = Patient.create("first_name", "Mary", "last_name", "Margolis");
        Patient p2 = Patient.create("first_name", "Mike", "last_name", "Henry");

        d1.add(p1);
        d2.add(p2);
        a(Base.count("shard1_doctors")).shouldBeEqual(2);
        a(Base.count("doctors")).shouldBeEqual(0); // we did not touch this table
        a(Base.count("patients")).shouldBeEqual(2);

        //test from the other side:
        List<Doctor> doctors = p2.getAll(Doctor.class);
        $(doctors.size()).shouldBeEqual(1);
        $(doctors.get(0).get("last_name")).shouldBeEqual("Hall");

        //******* now, lets switch the table **************
        Doctor.metaModel().setShardTableName("doctors");
        d1 = Doctor.createIt("first_name", "Greg", "last_name", "Tatham", "discipline", "surgeon");
        d2 = Doctor.createIt("first_name", "Hillary", "last_name", "Clinton", "discipline", "psychiatrist");

        //patients created in memory:
        p1 = Patient.create("first_name", "Mike", "last_name", "Myers");
        p2 = Patient.create("first_name", "Lenny", "last_name", "Kravitz");

        d1.add(p1);
        d2.add(p2);
        a(Base.count("shard1_doctors")).shouldBeEqual(2);
        a(Base.count("doctors")).shouldBeEqual(2);
        a(Base.count("patients")).shouldBeEqual(4);

        //test from the other side:
        doctors = p2.getAll(Doctor.class);
        $(doctors.size()).shouldBeEqual(1);
        $(doctors.get(0).get("last_name")).shouldBeEqual("Clinton");
    }

    //// POLYMORPHIC:
    @Test
    public void should_access_shard_child_in_polymorphic() {

        clearPolymorphic();

        Comment.metaModel().setShardTableName("shard1_comments");
        Article a = Article.createIt("title", "article 1", "content", "blah");
        a.add(Comment.create("author", "Igor Polevoy", "content", "this is just a test comment text"));
        a.add(Comment.create("author", "Ryan Kinderman", "content", "this is another test comment text"));
        List<Comment> comments = a.getAll(Comment.class).orderBy("id");

        a(comments.size()).shouldBeEqual(2);
        a(comments.get(0).get("author")).shouldBeEqual("Igor Polevoy");

        Post p = Post.createIt("title", "Post 1", "post", "tada");
        p.add(Comment.create("author", "jjohnes", "content", "this is just a test comment text"));
        p.add(Comment.create("author", "alapsi", "content", "this is another test comment text"));
        p.add(Comment.create("author", "kmandy", "content", "this is just a test comment text"));
        comments = p.getAll(Comment.class).orderBy("id");

        a(comments.size()).shouldBeEqual(3);
        a(comments.get(0).get("author")).shouldBeEqual("jjohnes");
        a(comments.get(1).get("author")).shouldBeEqual("alapsi");
        a(comments.get(2).get("author")).shouldBeEqual("kmandy");

        a(Comment.findAll().size()).shouldBeEqual(5);
        the(Base.count("shard1_comments")).shouldBeEqual(5);
        the(Base.count("comments")).shouldBeEqual(0);
    }

    @Test
    public void should_access_shard_parents_in_polymorphic() {
        clearPolymorphic();

        Article.metaModel().setShardTableName("shard1_articles");
        Post.metaModel().setShardTableName("shard1_posts");
        Article a = Article.createIt("title", "article 1", "content", "blah");
        a.add(Comment.create("author", "Igor Polevoy", "content", "this is just a test comment text"));
        a.add(Comment.create("author", "Ryan Kinderman", "content", "this is another test comment text"));
        List<Comment> comments = a.getAll(Comment.class).orderBy("id");

        a(comments.size()).shouldBeEqual(2);
        a(comments.get(0).get("author")).shouldBeEqual("Igor Polevoy");

        Post p = Post.createIt("title", "Post 1", "post", "tada");
        p.add(Comment.create("author", "jjohnes", "content", "this is just a test comment text"));
        p.add(Comment.create("author", "alapsi", "content", "this is another test comment text"));
        p.add(Comment.create("author", "kmandy", "content", "this is just a test comment text"));
        comments = p.getAll(Comment.class).orderBy("id");

        a(comments.size()).shouldBeEqual(3);
        a(comments.get(0).get("author")).shouldBeEqual("jjohnes");
        a(comments.get(1).get("author")).shouldBeEqual("alapsi");
        a(comments.get(2).get("author")).shouldBeEqual("kmandy");

        a(Comment.findAll().size()).shouldBeEqual(5);
        the(Base.count("shard1_articles")).shouldBeEqual(1);
        the(Base.count("articles")).shouldBeEqual(0);
        the(Base.count("shard1_posts")).shouldBeEqual(1);
        the(Base.count("posts")).shouldBeEqual(0);
    }

    //Utility methods:
    private void setupStandalone(){
        //base table:
        Base.exec("insert into temperatures (temp) values (10)");
        Base.exec("insert into temperatures (temp) values (20)");

        //shard 1:
        Base.exec("insert into shard1_temperatures (temp) values (30)");
        Base.exec("insert into shard1_temperatures (temp) values (40)");
        Base.exec("insert into shard1_temperatures (temp) values (50)");

        //shard 2:
        Base.exec("insert into shard2_temperatures (temp) values (60)");
        Base.exec("insert into shard2_temperatures (temp) values (70)");
        Base.exec("insert into shard2_temperatures (temp) values (80)");
        Base.exec("insert into shard2_temperatures (temp) values (90)");
    }

    private void clearPolymorphic(){
        Base.exec("delete from articles");
        Base.exec("delete from posts");
        Base.exec("delete from comments");
    }

    private void clearMany2Many(){
        Base.exec("delete from doctors");
        Base.exec("delete from patients");
        Base.exec("delete from doctors_patients");
    }
}
