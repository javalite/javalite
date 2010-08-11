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

import activejdbc.associations.BelongsToAssociation;
import activejdbc.associations.Many2ManyAssociation;
import activejdbc.associations.OneToManyAssociation;
import activejdbc.test.ActiveJDBCTest;
import org.junit.Test;



/**
 * @author Igor Polevoy
 */
public class AssociationEqualityTest extends ActiveJDBCTest {

    @Test
    public void testOneToManyEquals(){
        OneToManyAssociation one = new OneToManyAssociation("hello", "world", "hello_id");
        OneToManyAssociation two = new OneToManyAssociation("hello", "world", "hello_id");

        a(one).shouldBeEqual(two);

        OneToManyAssociation three = new OneToManyAssociation("hello", "world", "hi_id");
        a(one).shouldNotBeEqual(three);
    }

    @Test
    public void testBelongsToEquals(){
        BelongsToAssociation one = new BelongsToAssociation("hello", "world", "hello_id");
        BelongsToAssociation two = new BelongsToAssociation("hello", "world", "hello_id");

        a(one).shouldBeEqual(two);

        BelongsToAssociation three = new BelongsToAssociation("hello", "world", "hi_id");
        a(one).shouldNotBeEqual(three);
    }

    @Test
    public void testMany2ManyEquals(){
        Many2ManyAssociation one = new Many2ManyAssociation("hello", "world", "join", "hello_id", "world_id");
        Many2ManyAssociation two = new Many2ManyAssociation("hello", "world", "join", "hello_id", "world_id");

        a(one).shouldBeEqual(two);

        Many2ManyAssociation three = new Many2ManyAssociation("hello", "world", "join", "hi_id", "world_id");
        a(one).shouldNotBeEqual(three);
    }
}
