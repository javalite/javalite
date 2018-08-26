package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Account;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

public class ModelIsModifiedTest extends ActiveJDBCTest {


    @Test
    public void testModelIsModifiedPerson(){
        deleteAndPopulateTable("people");
        Person p = new Person();
        p.set("name", "Marilyn");
        p.set("last_name", "Monroe");
        p.set("dob", "1935-12-06");
        a(p.isModified()).shouldBeTrue();
        p.saveIt();

        Person f = Person.findFirst("name = ?", "Marilyn");
        f.set("last_name", "Monroe");
        a(f.isModified()).shouldBeFalse();
        f.set("last_name", "Kennedy");
        a(f.isModified()).shouldBeTrue();

        f = Person.findFirst("name = ?", "Marilyn");
        f.set("dob", "1935-12-06");
        a(f.isModified()).shouldBeFalse();
        f.set("dob", "1955-12-06");
        a(f.isModified()).shouldBeTrue();
    }

    @Test
    public void testModelIsModifiedAccount(){
        deleteAndPopulateTable("accounts");
        Account a = new Account();
        a.set("account", "my first account");
        a.set("amount", 5000.55);
        a(a.isModified()).shouldBeTrue();
        a.saveIt();

        Account f = Account.findFirst("account = ?", "my first account");
        f.set("amount", 5000.55);
        a(f.isModified()).shouldBeFalse();
        f.set("amount", 6000.35);
        a(f.isModified()).shouldBeTrue();

        f = Account.findFirst("account = ?", "my first account");
        f.set("total", 300000.15);
        a(f.isModified()).shouldBeTrue();
    }
}
