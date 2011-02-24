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
import activejdbc.test_models.Address;
import activejdbc.test_models.Person;
import activejdbc.test_models.User;
import javalite.test.XPathHelper;
import org.junit.Test;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class ToXmlSpec extends ActiveJDBCTest {

    @Test
    public void shouldGenerateSimpleXml(){
        resetTable("people");
        Person p  = (Person)Person.findById(1);
        String xml = p.toXml(2, true);
        System.out.println(xml);
        a(XPathHelper.selectText("//name", xml)).shouldEqual("John");
        a(XPathHelper.selectText("//last_name", xml)).shouldEqual("Smith");
    }

    @Test
    public void shouldIncludeChildren(){
        resetTables("users", "addresses");
        List<User> personList = User.findAll().orderBy("id").include(Address.class);
        User u = personList.get(0);
        String xml = u.toXml(2, true);
        System.out.println(xml);
        a(XPathHelper.count("//address", xml)).shouldEqual(3);

    }

    @Test
    public void shouldIncludeOnlyProvidedAttributes(){
        resetTables("users", "addresses");

        User u = (User)User.findById(1);
        String xml = u.toXml(2, true, "email", "last_name");

        a(XPathHelper.count("/user/*", xml)).shouldEqual(2);
        a(XPathHelper.selectText("/user/email", xml)).shouldEqual("mmonroe@yahoo.com");
        a(XPathHelper.selectText("/user/last_name", xml)).shouldEqual("Monroe");
    }

    @Test
    public void shouldGenerateFromList(){
        resetTables("users", "addresses");
        LazyList<User> personList = User.findAll().orderBy("id").include(Address.class);

        String xml = personList.toXml(2, true);
        System.out.println(xml);

        a(XPathHelper.count("//user", xml)).shouldEqual(2);
        a(XPathHelper.count("//address", xml)).shouldEqual(7);
        a(XPathHelper.count("//address2", xml)).shouldEqual(7);
    }
}
