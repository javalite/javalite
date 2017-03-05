package org.javalite.activejdbc;

import java.util.Collection;
import java.util.List;
import org.javalite.activejdbc.associations.Many2ManyAssociation;
import org.javalite.activejdbc.associations.OneToManyAssociation;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Doctor;
import org.javalite.activejdbc.test_models.DoctorsPatients;
import org.javalite.activejdbc.test_models.Patient;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class AttributesAndAssociationsTest extends ActiveJDBCTest{

    @Test
    public void shouldReturnCorrectAttributes(){

        Collection<String> attributes = Person.attributeNames();
        a(attributes.contains("name")).shouldBeTrue();
        a(attributes.contains("last_name")).shouldBeTrue();
        a(attributes.contains("graduation_date")).shouldBeTrue();
        a(attributes.contains("dob")).shouldBeTrue();
        a(attributes.contains("id")).shouldBeTrue();
        a(attributes.contains("created_at")).shouldBeTrue();
        a(attributes.contains("updated_at")).shouldBeTrue();
    }

    @Test
    public void shouldReturnCaseInsensitiveAttributes(){

        Collection<String> attributes = Person.attributeNames();
        a(attributes.contains("Name")).shouldBeTrue();
        a(attributes.contains("LAST_NAME")).shouldBeTrue();
        a(attributes.contains("graduation_date")).shouldBeTrue();
        a(attributes.contains("Dob")).shouldBeTrue();
        a(attributes.contains("ID")).shouldBeTrue();
        a(attributes.contains("Created_At")).shouldBeTrue();
        a(attributes.contains("updated_at")).shouldBeTrue();
    }

    @Test
    public void shouldReturnCorrectAssociations(){
        List<Association> associations = Doctor.associations();

        a(associations.get(0).getSourceClass()).shouldBeEqual(Doctor.class);
        a(associations.get(0).getTargetClass()).shouldBeEqual(DoctorsPatients.class);
        a(associations.get(0) instanceof OneToManyAssociation).shouldBeTrue();

        a(associations.get(1).getSourceClass()).shouldBeEqual(Doctor.class);
        a(associations.get(1).getTargetClass()).shouldBeEqual(Patient.class);
        a(associations.get(1) instanceof Many2ManyAssociation).shouldBeTrue();
    }
}
