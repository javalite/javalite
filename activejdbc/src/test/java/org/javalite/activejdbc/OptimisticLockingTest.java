package org.javalite.activejdbc;

import org.javalite.activejdbc.StaleModelException;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Watermelon;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class OptimisticLockingTest extends ActiveJDBCTest {

    @Test
    public void shouldSetVersionToOneWhenCreatingNewRecord(){
        deleteAndPopulateTable("watermelons");
        Watermelon m = new Watermelon();
        m.set("melon_type", "dark_green");
        m.saveIt();
        Watermelon m1 = Watermelon.findById(1);
        a(m1.get("record_version")).shouldBeEqual(1);
    }


    @Test
    public void shouldAdvanceVersionWhenRecordIsUpdated(){

        deleteAndPopulateTable("watermelons");
        Watermelon m = new Watermelon();
        m.set("melon_type", "dark_green");
        m.saveIt();

        m = Watermelon.findById(1);
        m.set("melon_type", "red").saveIt();
        a(m.get("record_version")).shouldBeEqual(2);// this will ensure that the value is updated in the model itself
        m = Watermelon.findById(1);
        a(m.get("record_version")).shouldBeEqual(2);

        m = Watermelon.findById(1);
        m.set("melon_type", "green").saveIt();
        m = Watermelon.findById(1);
        a(m.get("record_version")).shouldBeEqual(3);

        m = Watermelon.findById(1);
        m.set("melon_type", "yellow").saveIt();
        m = Watermelon.findById(1);
        a(m.get("record_version")).shouldBeEqual(4);
    }

    @Test(expected = StaleModelException.class)
    public void shouldThrowExceptionWhenVersionCollisionHappens(){
        deleteAndPopulateTable("watermelons");
        Watermelon m = new Watermelon();
        m.set("melon_type", "dark_green");
        m.saveIt();

        Watermelon m1 = Watermelon.findById(1);
        Watermelon m2 = Watermelon.findById(1);

        m1.set("melon_type", "red");
        m1.saveIt();

        m2.set("melon_type", "yellow");
        m2.saveIt();  //<<<<================ this will cause the StaleModelException
    }



    @Test
    public void should(){
        deleteAndPopulateTable("watermelons");

        Watermelon m = new Watermelon();
        m.set("melon_type", "dark_green");
        m.saveIt();

        m.set("melon_type", "light_green");
        m.saveIt();

        m.set("melon_type", "super_green");
        m.saveIt();

        m.set("melon_type", "dark_red");
        m.saveIt();
    }
}
