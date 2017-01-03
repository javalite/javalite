package org.javalite.activejdbc.test;

import org.javalite.activejdbc.Base;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * WARNING: all methods in this test need to be executed sequentially in order to succeed.
 * They depend on each other.
 *
 * @author igor on 12/3/16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DBSpecTest extends DBSpec{

    @Test
    public void a_clean(){
        setRollback(false);
        Base.exec("delete from patients");
    }

    @Test
    public void b_shouldHaveConnectionToTestDB(){
        a(Base.connection()).shouldNotBeNull();
    }

    @Test
    public void c_shouldRollBackTransactionByDefault(){
        Base.exec("insert into patients (first_name, last_name) values ('Billie', 'Holiday')");
    }

    @Test
    public void d_shouldNotFindRecordsFromRolledBackTransaction(){
        a(Base.count("patients")).shouldBeEqual(0);
    }

    @Test
    public void e_should_NOT_RollBackTransaction(){
        setRollback(false); //  here, we are leaving data in DB for the next test to find (no rollback at the end of test)
        Base.exec("insert into patients (first_name, last_name) values ('Billie', 'Holiday')");
    }

    @Test
    public void f_shouldFindRecordsFromPrevious_NOT_Rolledback_Transaction(){
        a(Base.count("patients")).shouldBeEqual(1);

        //lets cleanup after ourselves just in case
        setRollback(false);
        Base.exec("delete from patients");
    }
}
