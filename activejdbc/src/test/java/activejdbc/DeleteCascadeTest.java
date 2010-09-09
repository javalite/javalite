package activejdbc;

import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.Address;
import activejdbc.test_models.User;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class DeleteCascadeTest extends ActiveJDBCTest{

    @Test 
    public void shouldDeleteOnlyRelatedChildren(){
        resetTables("users", "addresses");

        //verify total count before delete
        a(Address.findAll().size()).shouldBeEqual(7);

        User.findById(1).deleteCascade();

        //verify total count after delete
        a(Address.findAll().size()).shouldBeEqual(4);
        
        //verify that no relations left in child table
        a(Address.where("user_id = ?", 1).size()).shouldBeEqual(0);
    }
}
