package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Account;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.common.Convert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.javalite.activejdbc.test.JdbcProperties.url;
import static org.javalite.common.Collections.map;

public class ModelModificationSpec extends ActiveJDBCTest {


    @Test
    public void shouldModifyString(){
        deleteAndPopulateTable("people");
        Person p = new Person();
        p.set("name", "Marilyn", "last_name", "Monroe", "dob", "1935-12-06");
        a(p.isModified()).shouldBeTrue();
        p.saveIt();

        Person person = Person.findFirst("name = ?", "Marilyn");

        person.set("last_name", "Monroe");
        the(person).shouldNotBe("modified");

        person.set("last_name", "Kennedy");
        the(person).shouldBe("modified");
    }


    @Test
    public void shouldModifyBigDecimal(){

        if(url().contains("mysql") || url().contains("h2")){
            deleteAndPopulateTable("accounts");
            Account account = new Account();
            account.set("account", "my first account");
            account.set("amount", 5000.55);
            the(account).shouldBe("modified");

            account = Account.findFirst("account = ?", "123");
            account.set("amount", Convert.toBigDecimal(9999.99));
            the(account).shouldNotBe("modified");
            account.set("amount", 6000.35);
            the(account).shouldBe("modified");
        }
    }

    @Test
    public void should_not_modify_if_Date_object_set_with_same_value() throws ParseException {

        if(url().contains("mysql") || url().contains("h2")){ //only run for MySQL and H2 because of the say they process java.sql.Date.

            deleteAndPopulateTable("people");
            Person person = new Person();
            Map<String, Object> inputMap = map("name", "Marilyn", "last_name", "Monroe","dob", "1935-12-06");

            person.fromMap(inputMap);
            the(person).shouldBe("modified");
            person.saveIt();

            person = Person.findFirst("name = ?", "Marilyn");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            Date d = df.parse("1935-12-06");
            inputMap = map("name", "Marilyn", "last_name", "Monroe","dob", d);
            person.fromMap(inputMap);
            the(person).shouldNotBe("modified");
        }
    }

}
