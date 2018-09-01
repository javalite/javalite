package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Account;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
    }

    @Test
    public void testModelIsModifiedPersonDate(){
        deleteAndPopulateTable("people");
        Person p = new Person();
        p.set("name", "Marilyn");
        p.set("last_name", "Monroe");
        p.set("dob", "1935-12-06");
        a(p.isModified()).shouldBeTrue();
        p.saveIt();

        Person u = Person.findFirst("name = ?", "Marilyn");
        u.set("dob", "1935-12-06");
        a(u.isModified()).shouldBeFalse();
        u.set("dob", "1955-12-06");
        a(u.isModified()).shouldBeTrue();
    }

    @Test
    public void testModelIsModifiedAccount(){
        deleteAndPopulateTable("accounts");
        Account a = new Account();
        a.set("account", "my first account");
        a.set("amount", 5000.55);
        a(a.isModified()).shouldBeTrue();

        Account f = Account.findFirst("account = ?", "123");
        f.set("amount", 9999.99);
        a(f.isModified()).shouldBeFalse();
        f.set("amount", 6000.35);
        a(f.isModified()).shouldBeTrue();
    }

    @Test
    public void testModelIsNotModifiedHydratePerson(){

        deleteAndPopulateTable("people");
        Person p = new Person();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("name", "Marilyn");
        inputMap.put("last_name", "Monroe");
        inputMap.put("dob", "1935-12-06");
        p.fromMap(inputMap);
        a(p.isModified()).shouldBeTrue();
        p.saveIt();

        Person f = Person.findFirst("name = ?", "Marilyn");
        inputMap = new HashMap<>();
        inputMap.put("name", "Marilyn");
        inputMap.put("last_name", "Monroe");
        inputMap.put("dob", "1935-12-06");
        f.fromMap(inputMap);
        a(f.isModified()).shouldBeFalse();
    }

    @Test
    public void testModelIsModifiedHydratePerson(){

        deleteAndPopulateTable("people");
        Person p = new Person();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("name", "Marilyn");
        inputMap.put("last_name", "Monroe");
        inputMap.put("dob", "1935-12-06");
        p.fromMap(inputMap);
        a(p.isModified()).shouldBeTrue();
        p.saveIt();

        Person f = Person.findFirst("name = ?", "Marilyn");
        inputMap = new HashMap<>();
        inputMap.put("name", "Marilyn");
        inputMap.put("last_name", "Kennedy");
        inputMap.put("dob", "1955-12-06");
        f.fromMap(inputMap);
        a(f.isModified()).shouldBeTrue();
    }
}
