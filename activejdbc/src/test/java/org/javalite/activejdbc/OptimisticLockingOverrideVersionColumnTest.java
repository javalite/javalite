package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Item;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class OptimisticLockingOverrideVersionColumnTest extends ActiveJDBCTest {

    @Test
    public void shouldSetVersionToOneWhenCreatingNewRecord(){
        deleteAndPopulateTable("items");
        Item m = new Item();
        m.set("item_number", 1);
        m.set("item_description", "descr 1");
        m.saveIt();
        Item m1 = Item.findById(1);
        a(m1.get("lock_version")).shouldBeEqual(1);
    }


    @Test
    public void shouldAdvanceVersionWhenRecordIsUpdated(){
        deleteAndPopulateTable("items");
        Item m = new Item();
        m.set("item_number", 1);
        m.set("item_description", "descr 1");
        m.saveIt();

        m = Item.findById(1);
        m.set("item_description", "descr 2").saveIt();
        a(m.get("lock_version")).shouldBeEqual(2);// this will ensure that the value is updated in the model itself
        m = Item.findById(1);
        a(m.get("lock_version")).shouldBeEqual(2);

        m = Item.findById(1);
        m.set("item_description", "descr 3").saveIt();
        m = Item.findById(1);
        a(m.get("lock_version")).shouldBeEqual(3);

    }

    @Test(expected = StaleModelException.class)
    public void shouldThrowExceptionWhenVersionCollisionHappens(){
        deleteAndPopulateTable("items");
        Item m = new Item();
        m.set("item_number", 1);
        m.set("item_description", "descr 2").saveIt();
        m.saveIt();

        Item m1 = Item.findById(1);
        Item m2 = Item.findById(1);

        m1.set("item_description", "descr 5").saveIt();
        m2.set("item_description", "descr 6").saveIt(); //<<<<================ this will cause the StaleModelException
    }
}
