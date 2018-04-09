package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Employee;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author igor on 4/7/18.
 */
public class ScopeSpec extends ActiveJDBCTest {

    @Before
    public void setup() {
        Employee.createIt("first_name", "John", "last_name", "Tibur", "position", "VP", "active", 1, "department", "IT");
        Employee.createIt("first_name", "Jane", "last_name", "Calista", "position", "Developer", "active", 1, "department", "IT");
        Employee.createIt("first_name", "Mike", "last_name", "Caret", "position", "Developer", "active", 0, "department", "IT");
    }

    @Test
    public void shouldUseBasicScope() {

        List<Employee> developers = Employee.scope("developers").all().orderBy("id");

        the(developers.size()).shouldEqual(2);
        the(developers.get(0).get("first_name")).shouldEqual("Jane");
        the(developers.get(1).get("first_name")).shouldEqual("Mike");
    }

    @Test
    public void shouldUseMultipleScopes() {
        List<Employee> active = Employee.scopes("developers", "active").all();
        the(active.size()).shouldEqual(1);
        the(active.get(0).get("first_name")).shouldEqual("Jane");
    }

    @Test(expected = DBException.class)
    public void shouldRejectNonExistentScope() {
        Employee.scope("does-not-exist").all();
    }

    @Test
    public void shouldCombineScopesAndConditions() {
        List<Employee> activeIT = Employee.scopes("IT", "active").where("last_name like ?", "%i%").orderBy("id");

        the(activeIT.size()).shouldEqual(2);
        the(activeIT.get(0).get("last_name")).shouldEqual("Tibur");
        the(activeIT.get(1).get("last_name")).shouldEqual("Calista");
    }
}
