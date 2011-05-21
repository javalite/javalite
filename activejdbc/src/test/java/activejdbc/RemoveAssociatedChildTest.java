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
import activejdbc.test_models.Book;
import activejdbc.test_models.Doctor;
import activejdbc.test_models.Library;
import activejdbc.test_models.Patient;
import org.junit.Test;

import java.util.List;
import java.util.Map;


/**
 * @author Igor Polevoy
 */
public class RemoveAssociatedChildTest extends ActiveJDBCTest {

    @Test
    public void testOne2many(){
        deleteAndPopulateTables("libraries", "books");
        Library l = (Library)Library.findById(1);
        List<Book> books = l.getAll(Book.class);

        int size = books.size();
        Book book = books.get(0);
        l.remove(book);
        
        a(book).shouldBe("frozen");
        a(l.getAll(Book.class).size()).shouldBeEqual(size - 1);
    }

    @Test
    public void testMany2Many(){
        deleteAndPopulateTables("doctors", "patients", "doctors_patients");
        Doctor doctor = (Doctor)Doctor.findById(1);        
        List<Patient> patients = doctor.getAll(Patient.class);
        int initSize = patients.size();

        doctor.remove(patients.get(0));

        //one few association from the same doctor to patients.
        a(doctor.getAll(Patient.class).size()).shouldBeEqual(initSize - 1);

        //number of join records decreased by 1
        List<Map> doctorPatientsList = Base.findAll("select * from doctors_patients");
        a(doctorPatientsList.size()).shouldBeEqual(2);

        //the number of patients is not changing
        a(Patient.findAll().size()).shouldBeEqual(2);
    }
}
