/*
Copyright 2009-2019 Igor Polevoy

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
import org.javalite.activejdbc.test_models.Account;
import org.javalite.activejdbc.test_models.Page;
import org.javalite.activejdbc.test_models.Salary;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class NumericValidationSpec extends ActiveJDBCTest {
    @Before
    public void setup() throws Exception {
        deleteAndPopulateTable("accounts");
    }

    @Test
    public void shouldAcceptCorrectValueForTotal(){
        deleteAndPopulateTable("accounts");
        Account account = new Account();
        account.set("amount", 1);
        account.set("total", 1);
        a(account).shouldBe("valid");
    }

    @Test
    public void shouldRejectValueBelowMin(){
        Account account = new Account();
        account.set("amount", 1);
        account.set("total", -1);
        account.validate();
        the(account).shouldNotBe("valid");
        the(account.errors().get("total")).shouldBeEqual("incorrect 'total'");
    }

    @Test
    public void shouldRejectValueAboveMax(){
        Account account = new Account();
        account.set("amount", 1);
        account.set("total", 101);
        a(account).shouldNotBe("valid");
        the(account.errors().get("total")).shouldBeEqual("incorrect 'total'");
    }

    @Test
    public void shouldAcceptNullValue(){
        Account account = new Account();
        account.set("amount", 1);
        account.set("total", null);
        a(account).shouldBe("valid");
    }

    @Test
    public void shouldRejectNonIntegerValue(){
        Account account = new Account();
        account.set("amount", 1);
        account.set("total", 1.1);
        a(account).shouldNotBe("valid");
        the(account.errors().get("total")).shouldBeEqual("incorrect 'total'");
    }

    @Test
    public void shouldFixDefect66() {

        Page page = new Page();
		the(page).shouldNotBe("valid");

        a(page.errors().size()).shouldBeEqual(1);
        a(page.errors().get("word_count")).shouldBeEqual("'word_count' must be an integer greater than 10");


        page.set("word_count", 4);
        the(page).shouldNotBe("valid");
        the(page.errors().size()).shouldBeEqual(1);
        the(page.errors().get("word_count")).shouldBeEqual("'word_count' must be an integer greater than 10");


        page.set("word_count", 11);
        the(page).shouldBe("valid");
        the(page.errors().size()).shouldBeEqual(0);


        page.set("word_count", 21.2);
        the(page).shouldBe("valid");
        the(page.get("word_count")).shouldEqual(21); // <---- the conversion to integer lost a decimal point.
    }

    @Test
    public void should_fail_conversion_with_custom_message() {
        Account account = new Account();
        account.set("amount", 1);
        account.set("total", "blah");
        the(account).shouldNotBe("valid");
        the(account.errors().size()).shouldBeEqual(1);
        the(account.errors().get("total")).shouldBeEqual("incorrect 'total'");
    }

    @Test
    public void shouldTestCustomNumericFormat(){

        Salary salary = new Salary();
        salary.set("salary", "$1.00");
        the(salary).shouldBe("valid");
        salary.set("salary", "#1.00");
        the(salary).shouldNotBe("valid");
    }
}
