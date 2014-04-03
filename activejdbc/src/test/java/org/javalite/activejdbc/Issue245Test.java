package org.javalite.activejdbc;


import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Member;
import org.javalite.activejdbc.test_models.Membership;
import org.junit.Test;

import java.util.List;

public class Issue245Test extends ActiveJDBCTest {

    @Test
    public void shouldNotFail(){

        // The following SQL needs to execute before running test

//        Membership.findAll().dump();
        List<Membership> memberships = Membership.find("type = 1 AND parent = 1");

        System.out.println(memberships);
    }
}
