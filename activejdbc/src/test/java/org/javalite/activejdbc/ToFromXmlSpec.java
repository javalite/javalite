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
import org.javalite.test.XPathHelper;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Util.readResource;

/**
 * @author Igor Polevoy
 */
public class ToFromXmlSpec extends ActiveJDBCTest {

    @Test
    public void shouldGenerateSimpleXml(){
        deleteAndPopulateTable("people");
        Person p  = Person.findById(1);
        String xml = p.toXml(true, true);
        a(XPathHelper.selectText("//name", xml)).shouldEqual("John");
        a(XPathHelper.selectText("//last_name", xml)).shouldEqual("Smith");
    }

    @Test
    public void shouldParseAttributesFromXml() throws ParseException {
        deleteAndPopulateTables("people");

        Person p = new Person();
        String xml = readResource("/person.xml");
        p.fromXml(xml);
        p.saveIt();
        p.refresh();

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

        a(p.get("name")).shouldBeEqual("John");
        a(p.get("last_name")).shouldBeEqual("Doe");
        a(p.get("graduation_date")).shouldBeEqual(f.parse("1979-06-01"));
        a(p.get("dob")).shouldBeEqual(f.parse("1962-06-13"));
    }


    @Test
    public void shouldIncludeChildren(){
        deleteAndPopulateTables("users", "addresses");

        User.findById(1).add(Address.create("zip", 60606, "state", "IL", "address1", "123 Pine & Needles", "city", "Chicago"));

        List<User> personList = User.findAll().orderBy("id").include(Address.class);
        User u = personList.get(0);
        String xml = u.toXml(true, true);

        the(xml).shouldBeEqual("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<user>\n" +
                "  <id>1</id>\n" +
                "  <first_name>Marilyn</first_name>\n" +
                "  <email>mmonroe@yahoo.com</email>\n" +
                "  <last_name>Monroe</last_name>\n" +
                "  <addresses>\n" +
                "    <address>\n" +
                "      <id>1</id>\n" +
                "      <zip>60606</zip>\n" +
                "      <state>IL</state>\n" +
                "      <address1>123 Pine St.</address1>\n" +
                "      <user_id>1</user_id>\n" +
                "      <address2>apt 31</address2>\n" +
                "      <city>Springfield</city>\n" +
                "    </address>\n" +
                "    <address>\n" +
                "      <id>2</id>\n" +
                "      <zip>60606</zip>\n" +
                "      <state>IL</state>\n" +
                "      <address1>456 Brook St.</address1>\n" +
                "      <user_id>1</user_id>\n" +
                "      <address2>apt 21</address2>\n" +
                "      <city>Springfield</city>\n" +
                "    </address>\n" +
                "    <address>\n" +
                "      <id>3</id>\n" +
                "      <zip>60606</zip>\n" +
                "      <state>IL</state>\n" +
                "      <address1>23 Grove St.</address1>\n" +
                "      <user_id>1</user_id>\n" +
                "      <address2>apt 32</address2>\n" +
                "      <city>Springfield</city>\n" +
                "    </address>\n" +
                "    <address>\n" +
                "      <id>8</id>\n" +
                "      <zip>60606</zip>\n" +
                "      <state>IL</state>\n" +
                "      <address1>123 Pine &amp; Needles</address1>\n" +
                "      <user_id>1</user_id>\n" +
                "      <city>Chicago</city>\n" +
                "    </address>\n" +
                "  </addresses>\n" +
                "</user>\n");

        a(XPathHelper.count("//address", xml)).shouldEqual(4);
        the(xml).shouldContain(" <address1>123 Pine &amp; Needles</address1>");

        the(u.toXml(false, true)).shouldBeEqual(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<user><id>1</id><first_name>Marilyn</first_name><email>mmonroe@yahoo.com</email><last_name>Monroe</last_name><addresses><address><id>1</id><zip>60606</zip><state>IL</state><address1>123 Pine St.</address1><user_id>1</user_id><address2>apt 31</address2><city>Springfield</city></address><address><id>2</id><zip>60606</zip><state>IL</state><address1>456 Brook St.</address1><user_id>1</user_id><address2>apt 21</address2><city>Springfield</city></address><address><id>3</id><zip>60606</zip><state>IL</state><address1>23 Grove St.</address1><user_id>1</user_id><address2>apt 32</address2><city>Springfield</city></address><address><id>8</id><zip>60606</zip><state>IL</state><address1>123 Pine &amp; Needles</address1><user_id>1</user_id><city>Chicago</city></address></addresses></user>");
    }

    @Test
    public void shouldIncludeOnlyProvidedAttributes(){
        deleteAndPopulateTables("users", "addresses");

        User u = User.findById(1);
        String xml = u.toXml(true, true, "email", "last_name");

        a(XPathHelper.count("/user/*", xml)).shouldEqual(2);
        a(XPathHelper.selectText("/user/email", xml)).shouldEqual("mmonroe@yahoo.com");
        a(XPathHelper.selectText("/user/last_name", xml)).shouldEqual("Monroe");
    }

    @Test
    public void shouldGenerateFromList(){
        deleteAndPopulateTables("users", "addresses");
        LazyList<User> personList = User.findAll().orderBy("id").include(Address.class);

        String xml = personList.toXml(false, true);
        the(xml).shouldBeEqual(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><users>"
                + "<user><id>1</id><first_name>Marilyn</first_name><email>mmonroe@yahoo.com</email><last_name>Monroe</last_name><addresses><address><id>1</id><zip>60606</zip><state>IL</state><address1>123 Pine St.</address1><user_id>1</user_id><address2>apt 31</address2><city>Springfield</city></address><address><id>2</id><zip>60606</zip><state>IL</state><address1>456 Brook St.</address1><user_id>1</user_id><address2>apt 21</address2><city>Springfield</city></address><address><id>3</id><zip>60606</zip><state>IL</state><address1>23 Grove St.</address1><user_id>1</user_id><address2>apt 32</address2><city>Springfield</city></address></addresses></user>"
                + "<user><id>2</id><first_name>John</first_name><email>jdoe@gmail.com</email><last_name>Doe</last_name><addresses><address><id>4</id><zip>60606</zip><state>IL</state><address1>143 Madison St.</address1><user_id>2</user_id><address2>apt 34</address2><city>Springfield</city></address><address><id>5</id><zip>60606</zip><state>IL</state><address1>153 Creek St.</address1><user_id>2</user_id><address2>apt 35</address2><city>Springfield</city></address><address><id>6</id><zip>60606</zip><state>IL</state><address1>163 Gorge St.</address1><user_id>2</user_id><address2>apt 36</address2><city>Springfield</city></address><address><id>7</id><zip>60606</zip><state>IL</state><address1>173 Far Side.</address1><user_id>2</user_id><address2>apt 37</address2><city>Springfield</city></address></addresses></user>"
                + "</users>");

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
        String xml = p.toXml(true, true);
        a(XPathHelper.selectText("/person/test", xml)).shouldEqual("test content");
    }

    @Test
    public void shouldEscapeSpecialCharsInXMLContent(){
        deleteAndPopulateTable("people");
        Person p = Person.findById(1);
        p.set("last_name", "Smith & Wesson");
        String xml = p.toXml(true, true);
        a(XPathHelper.selectText("/person/last_name", xml)).shouldEqual("Smith & Wesson");
        the(xml).shouldContain("<last_name>Smith &amp; Wesson</last_name>");
    }
}
