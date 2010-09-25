package activejdbc;

import activejdbc.associations.Many2ManyAssociation;
import activejdbc.associations.OneToManyAssociation;
import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.Doctor;
import activejdbc.test_models.DoctorsPatients;
import activejdbc.test_models.Person;
import activejdbc.test_models.User;
import org.junit.Test;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class AttributesAndAssociationsTest extends ActiveJDBCTest{
    
    @Test
    public void shouldReturnCorrectAttributes(){

        List<String> attributes = Person.attributes();
        a(attributes.contains("name")).shouldBeTrue();
        a(attributes.contains("last_name")).shouldBeTrue();
        a(attributes.contains("graduation_date")).shouldBeTrue();
        a(attributes.contains("dob")).shouldBeTrue();
        a(attributes.contains("id")).shouldBeTrue();
        a(attributes.contains("created_at")).shouldBeTrue();
        a(attributes.contains("updated_at")).shouldBeTrue();
    }

    @Test
    public void shouldReturnCorrectAssociations(){
        List<Association> associations = Doctor.associations();

        a(associations.get(0).getSource()).shouldBeEqual("doctors");
        a(associations.get(0).getTarget()).shouldBeEqual("doctors_patients");
        a(associations.get(0) instanceof OneToManyAssociation).shouldBeTrue();

        a(associations.get(1).getSource()).shouldBeEqual("doctors");
        a(associations.get(1).getTarget()).shouldBeEqual("patients");
        a(associations.get(1) instanceof Many2ManyAssociation).shouldBeTrue();
    }
}
