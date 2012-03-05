package org.javalite.activeweb;

import org.javalite.activejdbc.DBException;
import org.junit.Test;

/**
 * @author Igor Polevoy: 3/5/12 11:19 AM
 */
public class Issue88Spec extends AppIntegrationSpec {


    @Test(expected = DBException.class)
    public void shouldNotWrapDBException(){
        controller("db_exception").get("index");
    }
}
