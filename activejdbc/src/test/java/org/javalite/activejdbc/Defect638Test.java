package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Student;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test condition for issue <a href="https://github.com/javalite/activejdbc/issues/638">#638</a>
 *
 * @author João Francisco Almeida on 20/05/2018.
 */
public class Defect638Test extends ActiveJDBCTest {

    @Test
    public void shouldNotThrowIllegalArgumentException() {
        Student student = new Student();
        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put("enrollment_date", ""); // enrollment_date as an empty datetime2 column

        // fromMap shouldn't attempt to parse empty dates to be consistent with most Dialect implementations
        student.fromMap(modelMap);
    }
}
