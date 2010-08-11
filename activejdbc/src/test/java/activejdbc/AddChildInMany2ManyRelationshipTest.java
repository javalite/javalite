/*
Copyright 2009-2010 Igor Polevoy 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/


package activejdbc;

import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.Doctor;
import activejdbc.test_models.Patient;
import org.junit.Test;

import java.util.List;
import java.util.Map;


/**
 * @author Igor Polevoy
 */
public class AddChildInMany2ManyRelationshipTest extends ActiveJDBCTest {

    @Test
    public void shouldAddNewChildInManyToManyAssociation(){
        resetTables("doctors", "patients", "doctors_patients");
        Doctor doctor = (Doctor)Doctor.findById(1);
        Patient jimThePatient = (Patient)Patient.create("first_name", "Jim", "last_name", "Smith");
        //this will add a new patient record and a new record in the join table that connects a doctor and a new patient.
        doctor.add(jimThePatient);
        List<Map> doctorPatientsList = Base.findAll("select * from doctors_patients order by id");
        a(doctorPatientsList.size()).shouldBeEqual(4);

        a(doctorPatientsList.get(3).get("doctor_id")).shouldBeEqual(1);
        a(doctorPatientsList.get(3).get("patient_id")).shouldBeEqual(3);

        List<Patient> patients = Patient.findAll().orderBy("id");
        a(patients.size()).shouldBeEqual(3);
        Patient jim = patients.get(2);
        a(jim.get("first_name")).shouldBeEqual("Jim");
        a(jim.get("last_name")).shouldBeEqual("Smith");
    }
}
