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


package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Many2Many;

/**
 * @author Igor Polevoy
 */
@Many2Many(other = Course.class, join = "registrations", sourceFKName = "astudent_id", targetFKName = "acourse_id")
public class Student extends Model {
    static {
        dateFormat("MM/dd/yyyy", "dob");
        timestampFormat("MM/dd/yyyy h a", "enrollment_date");
        blankToNull("first_name", "last_name");
    }
}
