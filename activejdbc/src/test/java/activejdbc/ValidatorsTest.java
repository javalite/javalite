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
import activejdbc.validation.ValidationException;
import javalite.test.jspec.ExceptionExpectation;
import activejdbc.test_models.*;
import org.junit.Test;


public class ValidatorsTest extends ActiveJDBCTest {

    @Test
    public void testPresenceValidator(){
        resetTable("people");
        Person p = new Person();
        p.validate();
        a(p.errors().size()).shouldBeEqual(2);
        a(p.errors().get("name")).shouldBeEqual("value is missing");
        a(p.errors().get("last_name")).shouldBeEqual("value is missing");

        p.set("name", "igor");
        p.validate();
        a(p.errors().size()).shouldBeEqual(1);

        p.set("last_name", "polevoy");
        p.validate();
        a(p.errors().size()).shouldBeEqual(0);        
    }

    @Test
    public void testNumericValidator(){
        resetTable("accounts");
        Account a = new Account();
        //try straight number
        a.set("amount", 1.2);
        a.validate();
        a(a.errors().size()).shouldBeEqual(0);

        //try not number
        a.set("amount", "hello");
        a.validate();
        a(a.errors().size()).shouldBeEqual(1);
        a(a.errors().get("amount")).shouldBeEqual("value is not a number");

        //try numeric string
        a.set("amount", "123");
        a.validate();
        a(a.errors().size()).shouldBeEqual(0);


        //try bad string with a number in it
        a.set("amount", "111 aaa");
        a.validate();
        a(a.errors().size()).shouldBeEqual(1);

//        //try null value with a validator.
//        a.set("amount", null);
//        a.validate();
//        a(a.errors().size()).shouldBeEqual(1);
//        a(a.errors().get("amount")).shouldBeEqual("value is not a number");
        
    }

    @Test
    public void testRangeValidator(){
        resetTable("temperatures");
        Temperature t = new Temperature();

        //specified value in range
        t.set("temp", 59);
        t.validate();
        a(t.errors().size()).shouldBeEqual(0);

        //specified value out of range
        t.set("temp", 200);
        t.validate();
        a(t.errors().size()).shouldBeEqual(1);
        a(t.errors().get("temp")).shouldBeEqual("temperature cannot be less than 0 or more than 100");

        //pass double, which is a different type than range values specified in the model, exception is thrown
        t.set("temp", 200.00);
        Exception ex = null;
        try{
            t.validate();
        }catch(Exception e){ex = e;}
        
        a(ex).shouldBeType(IllegalArgumentException.class);
    }

    @Test
    public void testMessagePassing(){
        resetTable("salaries");
        Salary s = new Salary();
        s.validate();
        a(s.errors().get("salary")).shouldBeEqual("salary is missing!!!");
    }

    @Test
    public void testRegexpValidator(){
        resetTables("users", "addresses");
        User u = new User();

        //test good value
        u.set("email", "igor@polevoy.org");
        u.validate();
        a(u.errors().size()).shouldBeEqual(0);

        //test bad value
        u.set("email", "this is not email value");
        u.validate();
        a(u.errors().size()).shouldBeEqual(1);
    }

    @Test
    public void testSaveItMethod(){
        resetTables("users", "addresses");
        final User u = new User();

        //cause exception
        u.set("email", "this is not email value");
        expect(new ExceptionExpectation(ValidationException.class) {
            public void exec() {
                u.saveIt();          
            }
        });
    }

    @Test
    public void shouldConvertEmptyStringToNull(){
        resetTables("items");

        Item it = new Item();
        it.set("item_number", "");
        it.validate();
        a(it.get("item_number")).shouldBeNull();
    }


    @Test
    public void shouldReturnNullIfRequestedErrorForAttributeWhichDidNotProduceOne(){
        resetTables("items");

        Item it = new Item();
        a(it.errors().get("blah")).shouldBeNull();
    }

    @Test
    public void shouldNotOverwritePreviousValidation(){
        resetTables("accounts");

        //first validator
        Account account = new Account();
        account.set("amount", "");
        account.validate();
        a(account.errors().get("amount")).shouldEqual("value is missing");

        //second validator
        account.set("amount", "hello");
        account.validate();
        a(account.errors().get("amount")).shouldEqual("value is not a number");

    }
}
