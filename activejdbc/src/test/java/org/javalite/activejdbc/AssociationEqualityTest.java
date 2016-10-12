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

import org.javalite.activejdbc.associations.BelongsToAssociation;
import org.javalite.activejdbc.associations.Many2ManyAssociation;
import org.javalite.activejdbc.associations.OneToManyAssociation;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.junit.Test;



/**
 * @author Igor Polevoy
 */
public class AssociationEqualityTest extends ActiveJDBCTest {

    @Test
    public void testOneToManyEquals(){
        OneToManyAssociation one = new OneToManyAssociation(User.class, Address.class, "user_id");
        OneToManyAssociation two = new OneToManyAssociation(User.class, Address.class, "user_id");

        a(one).shouldBeEqual(two);

        OneToManyAssociation three = new OneToManyAssociation(User.class, Address.class, "usr_id");
        a(one).shouldNotBeEqual(three);
    }

    @Test
    public void testBelongsToEquals(){
        BelongsToAssociation one = new BelongsToAssociation(Address.class, User.class, "user_id");
        BelongsToAssociation two = new BelongsToAssociation(Address.class, User.class, "user_id");

        a(one).shouldBeEqual(two);

        BelongsToAssociation three = new BelongsToAssociation(Address.class, User.class, "usr_id");
        a(one).shouldNotBeEqual(three);
    }

    @Test
    public void testMany2ManyEquals(){
        Many2ManyAssociation one = new Many2ManyAssociation(Doctor.class, Patient.class, "join", "doctor_id", "patient_id");
        Many2ManyAssociation two = new Many2ManyAssociation(Doctor.class, Patient.class, "join", "doctor_id", "patient_id");

        a(one).shouldBeEqual(two);

        Many2ManyAssociation three = new Many2ManyAssociation(Doctor.class, Patient.class, "join", "dctr_id", "ptnt_id");
        a(one).shouldNotBeEqual(three);
    }
}
