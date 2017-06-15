package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Box;
import org.javalite.activejdbc.test_models.Fruit;
import org.junit.Test;

import java.util.List;

/**
 * Models related in the database are unrelated by @UnrelatedTo annotation.
 *
 * @author igor on 5/31/17.
 */
public class UnrelatedSpec extends ActiveJDBCTest {

    @Test
    public void shouldUnrelatedRelatedModels(){
        List<Association> associations = Box.getMetaModel().getAssociationsForTarget(Fruit.class);
        the(associations.size()).shouldBeEqual(0);
    }
}
