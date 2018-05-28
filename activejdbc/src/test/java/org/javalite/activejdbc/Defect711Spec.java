package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Member;
import org.javalite.activejdbc.test_models.SkillExperience;
import org.junit.Test;

/**
 * @author igor on 5/28/18.
 */
public class Defect711Spec extends ActiveJDBCTest {

    @Test
    public void shouldNotThrowNPE(){

        Integer oldLevel = SkillExperience.getLevel("x", "y");

    }
}
