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
import org.javalite.activejdbc.test_models.Account;
import org.javalite.activejdbc.test_models.Page;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class NumericValidationBuilderTest extends ActiveJDBCTest {

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
        System.out.println(account.errors());
        a(account).shouldBe("valid");
    }

    @Test
    public void shouldRejectValueBelowMin(){
        Account account = new Account();
        account.set("amount", 1);
        account.set("total", -1);
        a(account).shouldNotBe("valid");
    }

    @Test
    public void shouldRejectValueAboveMax(){
        Account account = new Account();
        account.set("amount", 1);
        account.set("total", 101);
        a(account).shouldNotBe("valid");
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
    }

    @Test
    public void shouldFixDefect66() {

        Page page = new Page();
		a(page).shouldNotBe("valid");

        a(page.errors().size()).shouldBeEqual(1);
        a(page.errors().get("word_count")).shouldBeEqual("value is missing");
    }
}
