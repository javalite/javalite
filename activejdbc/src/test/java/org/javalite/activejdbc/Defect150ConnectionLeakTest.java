package org.javalite.activejdbc;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.After;
import org.junit.Test;

import static org.javalite.activejdbc.test.JdbcProperties.*;

/**
 * @author Igor Polevoy
 */
public class Defect150ConnectionLeakTest implements JSpecSupport {

    @After
    public void after(){
        DB.closeAllConnections();
    }

    @Test(expected = DBException.class)
    public void shouldThrowExceptionIfConnectionOpenedWithoutClosingPrevious() {
        Base.open(driver(), url(), user(), password());
        Base.open(driver(), url(), user(), password());
    }
}
