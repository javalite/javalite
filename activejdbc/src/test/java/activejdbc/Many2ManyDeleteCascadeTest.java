package activejdbc;

import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.Doctor;
import activejdbc.test_models.Patient;
import org.junit.Test;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class Many2ManyDeleteCascadeTest extends ActiveJDBCTest {

    @Test
    public void shouldRemoveJoinLinksWHenDeleted() {
        resetTables("doctors", "patients", "doctors_patients");

        Doctor.<Model>findAll().dump(System.out);
        System.out.println(Base.findAll("Select * from doctors_patients"));

        Doctor doctorNumberOne = (Doctor)Doctor.findById(1);
        doctorNumberOne.delete();

        a(Base.findAll("Select * from doctors_patients").size()).shouldBeEqual(1);
    }

}
