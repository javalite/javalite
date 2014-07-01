/*
Copyright 2009-2014 Igor Polevoy

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
import org.javalite.activejdbc.test_models.Address;
import org.javalite.activejdbc.test_models.Article;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.activejdbc.test_models.User;
import org.javalite.common.XmlEntities;
import org.javalite.test.XPathHelper;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class ToXmlSpec extends ActiveJDBCTest {

    @Test
    public void shouldGenerateSimpleXml(){
        deleteAndPopulateTable("people");
        Person p  = Person.findById(1);
        String xml = p.toXml(2, true);
        System.out.println(xml);
        a(XPathHelper.selectText("//name", xml)).shouldEqual("John");
        a(XPathHelper.selectText("//last_name", xml)).shouldEqual("Smith");
    }

    @Test
    public void shouldIncludeChildren(){
        deleteAndPopulateTables("users", "addresses");

        User.findById(1).add(Address.create("zip", 60606, "state", "IL", "address1", "123 Pine & Needles", "city", "Chicago"));

        List<User> personList = User.findAll().orderBy("id").include(Address.class);
        User u = personList.get(0);
        String xml = u.toXml(2, true);

        a(XPathHelper.count("//address", xml)).shouldEqual(4);
        the(xml).shouldContain(" <address1>123 Pine &amp; Needles</address1>");
    }

    @Test
    public void shouldIncludeOnlyProvidedAttributes(){
        deleteAndPopulateTables("users", "addresses");

        User u = User.findById(1);
        String xml = u.toXml(2, true, "email", "last_name");

        a(XPathHelper.count("/user/*", xml)).shouldEqual(2);
        a(XPathHelper.selectText("/user/email", xml)).shouldEqual("mmonroe@yahoo.com");
        a(XPathHelper.selectText("/user/last_name", xml)).shouldEqual("Monroe");
    }

    @Test
    public void shouldGenerateFromList(){
        deleteAndPopulateTables("users", "addresses");
        LazyList<User> personList = User.findAll().orderBy("id").include(Address.class);

        String xml = personList.toXml(2, true);
        System.out.println(xml);

        a(XPathHelper.count("//user", xml)).shouldEqual(2);
        a(XPathHelper.count("//address", xml)).shouldEqual(7);
        a(XPathHelper.count("//address2", xml)).shouldEqual(7);
    }

    @Test
    public void shouldConvertClobsToString(){
        deleteAndPopulateTable("articles");

        List<Map> maps = Article.findAll().toMaps();
        a(maps.get(0).get("content")).shouldBeA(String.class);
    }


    @Test
    public void shouldInjectCustomContentIntoXML(){
        deleteAndPopulateTable("people");
        Person p  = Person.findById(1);
        String xml = p.toXml(2, true);

        a(XPathHelper.selectText("/person/test", xml)).shouldEqual("test content");

    }

    @Test
    public void shouldEscapeSpecialCharsInXMLContent(){
        deleteAndPopulateTable("people");
        Person p  = Person.findById(1);


        p.set("last_name", "Smith & Wesson");


        String xml = p.toXml(2, true);

        System.out.println(xml);

        a(XPathHelper.selectText("/person/last_name", xml)).shouldEqual("Smith & Wesson");

        the(xml).shouldContain("<last_name>Smith &amp; Wesson</last_name>");



    }
}
