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

import activejdbc.associations.NotAssociatedException;
import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.*;

import org.junit.Test;

import java.sql.Date;
import java.util.List;
import java.util.Map;


/**
 * @author Igor Polevoy
 */
public class Many2ManyRelationshipTest extends ActiveJDBCTest {

    @Test
    public void shouldSelectManyToManyWithGetAll(){
        resetTables("doctors", "patients", "doctors_patients");
        Doctor doctor = (Doctor)Doctor.findById(1);
        List<Patient> patients = doctor.getAll(Patient.class);
        a(2).shouldBeEqual(patients.size());

        doctor = (Doctor)Doctor.findById(2);
        patients = doctor.getAll(Patient.class);
        a(1).shouldBeEqual(patients.size());

        Patient p = (Patient)Patient.findById(1);
        List<Doctor> doctors = p.getAll(Doctor.class);
        a(2).shouldBeEqual(doctors.size());

        p = (Patient)Patient.findById(2);
        doctors = p.getAll(Doctor.class);
        a(1).shouldBeEqual(doctors.size());
    }

    @Test
    public void shouldFindManyToManyViaGetter(){
        resetTables("doctors", "patients", "doctors_patients");
        Doctor doctor = (Doctor)Doctor.findById(1);
        List<Patient> patients = (List<Patient>)doctor.get("patients");
        a(2).shouldBeEqual(patients.size());
    }

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

    @Test(expected = NotAssociatedException.class)
     public void shouldRejectUnassociatedModel(){
        resetTables("students");

        Student bill = (Student)Student.createIt("dob", new Date(System.currentTimeMillis()), "first_name", "Bill", "last_name", "Jansen");
        bill.add(Plant.<Model>create("plant_name", "pine", "category", "trees"));
     }

    @Test
    public void shouldFindWithParamsForJoinTable(){

        Programmer programmer = (Programmer)Programmer.createIt("first_name", "Jim", "last_name", "Garnoe");
        Project project1 = (Project)Project.createIt("project_name", "Prove theory of everything");
        Project project2 = (Project)Project.createIt("project_name", "Find meaning of life");

        Assignment assignment = (Assignment)Assignment.createIt("duration_weeks", 3);
        programmer.add(assignment);
        project1.add(assignment);
        
        programmer.add(project2);

        List<Project> projects = programmer.get(Project.class, "duration_weeks = ?", 3);
        a(projects.size()).shouldBeEqual(1);
    }
}
