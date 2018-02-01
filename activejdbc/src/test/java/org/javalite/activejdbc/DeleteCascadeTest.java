package org.javalite.activejdbc;

import org.javalite.activejdbc.cache.CacheEvent;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;


/**
 * @author Igor Polevoy
 */
public class DeleteCascadeTest extends ActiveJDBCTest{

    @Test 
    public void shouldDeleteOneToManyDeep(){
        deleteAndPopulateTables("users", "addresses", "rooms");

        //verify child count
        a(Address.count()).shouldBeEqual(7);
        //verify grand children count
        a(Room.count()).shouldBeEqual(4);

        //delete
        User u = User.findById(1);
        u.deleteCascade();
        a(u).shouldBe("frozen");
        a(User.count()).shouldBeEqual(2);

        //verify total count after delete
        a(Address.count()).shouldBeEqual(4);
        
        //verify that no relations left in child table
        a(Address.where("user_id = ?", 1).size()).shouldBeEqual(0);

        //check deep delete here
        a(Room.count()).shouldBeEqual(2);
        a(Room.count("address_id= ?", 1)).shouldBeEqual(0);
    }

    /**
     * This is to test conventional models
     */
    @Test
    public void shouldDeletePolymorphicChildren(){

        deleteAndPopulateTables("articles", "posts", "comments");
        Article a = Article.findById(1);
        a.add(Comment.create("author", "ipolevoy", "content", "this is just a test comment text"));
        a.add(Comment.create("author", "rkinderman", "content", "this is another test comment text"));


        Post p = Post.findById(2);
        p.add(Comment.create("author", "jjohnes", "content", "this is just a test comment text"));
        p.add(Comment.create("author", "alapsi", "content", "this is another test comment text"));
        p.add(Comment.create("author", "kmandy", "content", "this is just a test comment text"));

        a(Comment.findAll().size()).shouldBeEqual(5);
        a.deleteCascade();
        a(Comment.findAll().size()).shouldBeEqual(3);
        a(Comment.where("parent_type = ? and parent_id = ?", "activejdbc.test_models.Article", 1).size()).shouldBeEqual(0);
    }

    /**
     * This is to test models with annotations that override conventions.
     */
    @Test
    public void shouldDeletePolymorphicChildrenDeep(){
        deleteAndPopulateTables("vehicles", "mammals", "classifications");
        Vehicle car = Vehicle.createIt("name", "car");
        Classification fourWheeled = Classification.create("name", "four wheeled");
        car.add(fourWheeled);
        Classification sedan = Classification.create("name", "sedan");
        car.add(sedan);
        sedan.add(SubClassification.create("name", "passenger"));

        a(SubClassification.count()).shouldBeEqual(1);
        car.deleteCascade();
        a(SubClassification.count()).shouldBeEqual(0);
    }


    @Test
    public void shouldDeleteMany2ManyDeep() {

        //case 1: simple: follow many to many, then one to many
        deleteAndPopulateTables("doctors", "patients", "doctors_patients", "prescriptions");
        Registry.cacheManager().flush(CacheEvent.ALL);

        a(Prescription.count()).shouldBeEqual(5);

        Doctor.findById(3).deleteCascade();

        a(Doctor.count()).shouldBeEqual(3);
        a(DoctorsPatients.count()).shouldBeEqual(3);
        a(Prescription.count()).shouldBeEqual(4);

        //case 2: more complicated, the search comes back to DOCTORS table and removes another doctor.
        deleteAndPopulateTables("doctors", "patients", "doctors_patients", "prescriptions", "comments");
        Prescription.findById(1).add(Comment.create("author", "doctor", "content", "live live to the fullest"));
        Prescription.findById(5).add(Comment.create("author", "doctor", "content", "make cancer go away!"));

        a(Prescription.count()).shouldBeEqual(5);
        a(Comment.count()).shouldBeEqual(2);

        Doctor.findById(1).deleteCascade();

        a(Doctor.count()).shouldBeEqual(2);
        a(DoctorsPatients.count()).shouldBeEqual(1);
        a(Prescription.count()).shouldBeEqual(1);
        a(Comment.count()).shouldBeEqual(1);
    }


    @Test
    public void shouldDeleteMany2ManyDeepSkippingAssociation() {

        deleteAndPopulateTables("doctors", "patients", "doctors_patients", "prescriptions");
        Registry.cacheManager().flush(new CacheEvent("doctors", ""));
        Registry.cacheManager().flush(new CacheEvent("patients", ""));
        Registry.cacheManager().flush(new CacheEvent("prescriptions", ""));



        Doctor.findAll().dump();
        Patient.findAll().dump();
        Prescription.findAll().dump();

        a(Doctor.count()).shouldBeEqual(4);
        a(Patient.count()).shouldBeEqual(3);
        a(Prescription.count()).shouldBeEqual(5);

        Patient.findById(3).deleteCascadeExcept(Patient.getMetaModel().getAssociationForTarget(Prescription.class));

        a(Doctor.count()).shouldBeEqual(3);
        a(Patient.count()).shouldBeEqual(2);
        a(Prescription.count()).shouldBeEqual(5); //<<< Prescription count is unchanged!
    }


    @Test
    public void shouldDeleteOne2ManyShallow(){
        deleteAndPopulateTables("users", "addresses", "rooms");

        //verify child count
        a(Address.count()).shouldBeEqual(7);
        //verify grand children count
        a(Room.count()).shouldBeEqual(4);

        //delete
        User u = User.findById(1);
        u.deleteCascadeShallow();
        a(u).shouldBe("frozen");
        a(User.count()).shouldBeEqual(2);

        //verify total count after delete
        a(Address.count()).shouldBeEqual(4);

        //verify that no relations left in child table
        a(Address.where("user_id = ?", 1).size()).shouldBeEqual(0);

        //check shallow delete here - there needs to be the same number as before.
        a(Room.count()).shouldBeEqual(4);
        //and we have orphan rooms, but I guess this is expected.s
        a(Room.count("address_id= ?", 1)).shouldBeEqual(2);
    }

    @Test
    public void shouldDeleteMany2ManyShallow(){
        deleteAndPopulateTables("doctors", "patients", "doctors_patients", "prescriptions");
        Registry.cacheManager().flush(CacheEvent.ALL);

        a(Prescription.count()).shouldBeEqual(5);

        Doctor.findById(3).deleteCascadeShallow();

        a(Doctor.count()).shouldBeEqual(3);
        a(DoctorsPatients.count()).shouldBeEqual(3);

        //so, prescriptions did not get deleted, hence orphaned
        a(Prescription.count()).shouldBeEqual(5);
    }

    @Test
    public void shouldDeletePolymorphicShallow(){

        SubClassification.deleteAll();

        deleteAndPopulateTables("vehicles", "mammals", "classifications");
        Registry.cacheManager().flush(CacheEvent.ALL);
        Vehicle car = Vehicle.createIt("name", "car");
        Classification fourWheeled = Classification.create("name", "four wheeled");
        car.add(fourWheeled);
        Classification sedan = Classification.create("name", "sedan");
        car.add(sedan);
        sedan.add(SubClassification.create("name", "passenger"));
        sedan.add(SubClassification.create("name", "4 wheel drive"));

        //pre-execution checks
        a(Vehicle.count()).shouldBeEqual(1);
        a(Classification.count()).shouldBeEqual(2);
        a(SubClassification.count()).shouldBeEqual(2);

        car.deleteCascadeShallow();

        //model and child deleted
        a(Vehicle.count()).shouldBeEqual(0);
        a(Classification.count()).shouldBeEqual(0);
        //sub-classification did not get deleted
        a(SubClassification.count()).shouldBeEqual(2);
    }
}
