package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Meal;
import org.junit.Test;

/**
 * @author Igor Polevoy: 3/6/12 12:51 AM
 */
public class Defect140Test extends ActiveJDBCTest {

    @Test
    public void shouldNotWrapDBException(){
        Meal m  = new Meal();
        try{
            m.saveIt();
        }catch(DBException e){
            the(e.getCause() instanceof DBException).shouldBeFalse();
        }
    }
}
