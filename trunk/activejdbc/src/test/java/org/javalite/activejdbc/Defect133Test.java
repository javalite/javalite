package org.javalite.activejdbc;

import org.junit.Test;

import static org.javalite.activejdbc.test.JdbcProperties.*;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy: 1/25/12 1:41 PM
 */
public class Defect133Test {

    @Test
    public void shouldNotFindConnectionOnThread(){

        a(Base.hasConnection()).shouldBeFalse();

    }

    @Test(expected = DBException.class)
    public void shouldThrowExceptionIfGettingNonExistentConnection(){

        Base.connection();
    }

    @Test
    public void shouldFindConnectionOnThread(){
        Base.open(driver(), url(), user(), password());

        a(Base.hasConnection()).shouldBeTrue();

        Base.close();
    }
}
