/*
Copyright 2009-2016 Igor Polevoy

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


package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Course;
import org.javalite.activejdbc.test_models.Student;
import org.junit.Test;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class Many2ManyOverrideConventionsTest extends ActiveJDBCTest {


    @Test
    public void test(){

        deleteAndPopulateTables("students", "courses", "registrations");
        
        Student student = Student.findById(1);
        List<Course> courses = student.getAll(Course.class);
        a(2).shouldBeEqual(courses.size());

        student = Student.findById(2);
        courses = student.getAll(Course.class);
        a(1).shouldBeEqual(courses.size());

        Course c = Course.findById(1);
        List<Student> Students = c.getAll(Student.class);
        a(2).shouldBeEqual(Students.size());

        c = Course.findById(2);
        Students = c.getAll(Student.class);
        a(1).shouldBeEqual(Students.size());
    }
}
