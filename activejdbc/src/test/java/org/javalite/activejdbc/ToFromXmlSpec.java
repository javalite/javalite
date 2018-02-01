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

import java.io.IOException;
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
import org.javalite.activejdbc.test_models.Comment;
import org.javalite.activejdbc.test_models.Tag;

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
        a(p.getDate("graduation_date")).shouldBeEqual(f.parse("1979-06-01"));
        a(p.getDate("dob")).shouldBeEqual(f.parse("1962-06-13"));
    }

    @Test
    public void shouldNotFailXmlNoValue() throws ParseException {
        deleteAndPopulateTables("people");

        Person p = new Person();
        String xml = readResource("/person_no_val.xml");
        p.fromXml(xml);
        p.saveIt();
        p.refresh();

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

        a(p.get("name")).shouldBeEqual("John");
        a(p.get("last_name")).shouldBeEqual("Doe");
        a(p.getDate("graduation_date")).shouldBeNull();
        a(p.getDate("dob")).shouldBeEqual(f.parse("1962-06-13"));
    }

    @Test
    public void shouldIncludeChildren(){
        deleteAndPopulateTables("users", "addresses");
        User.findById(1).add(Address.create("zip", 60606, "state", "IL", "address1", "123 Pine & Needles", "city", "Chicago"));
        List<User> personList = User.findAll().orderBy("id").include(Address.class);

        User u = personList.get(0);
        checkXmlStructure(u.toXml(true, true));
        checkXmlStructure(u.toXml(false, true));
    }

    private void checkXmlStructure(String xml){
        XPathHelper h = new XPathHelper(xml);
        the(h.selectText("//first_name")).shouldBeEqual("Marilyn");
        the(h.selectText("//last_name")).shouldBeEqual("Monroe");
        the(h.count("//address")).shouldBeEqual(4);
        the(h.selectText("//address[1]/address1")).shouldBeEqual("123 Pine St.");
        the(h.selectText("//address[2]/address1")).shouldBeEqual("456 Brook St.");
        the(h.selectText("//address[3]/address1")).shouldBeEqual("23 Grove St.");
        the(h.selectText("//address[4]/address1")).shouldBeEqual("123 Pine & Needles");
    }

    @Test
    public void shouldIncludeOnlyProvidedAttributes(){
        deleteAndPopulateTables("users", "addresses");

        User u = User.findById(1);
        String xml = u.toXml(true, true, "email", "last_name");

        a(XPathHelper.count("/user/*", xml)).shouldEqual(2);
        a(XPathHelper.selectText("/user/email", xml)).shouldEqual("mmonroe@yahoo.com");
        a(XPathHelper.selectText("/user/last_name", xml)).shouldEqual("Monroe");
        a(XPathHelper.count("//first_name", xml)).shouldEqual(0);
    }

    @Test
    public void shouldGenerateFromList(){
        deleteAndPopulateTables("users", "addresses");
        LazyList<User> personList = User.findAll().orderBy("id").include(Address.class);

        String xml = personList.toXml(false, true);

        a(XPathHelper.count("//user", xml)).shouldEqual(3);
        a(XPathHelper.count("//address", xml)).shouldEqual(7);
        a(XPathHelper.count("//address2", xml)).shouldEqual(7);
    }

    @Test
    public void shouldConvertClobsToString(){
        deleteAndPopulateTable("articles");

        List<Map<String, Object>> maps = Article.findAll().toMaps();
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
        Person p = Person.create("name", "John", "last_name", "Smith & Wesson");
        String xml = p.toXml(true, true);
        a(XPathHelper.selectText("/person/last_name", xml)).shouldEqual("Smith & Wesson");
        the(xml).shouldContain("<last_name>Smith &amp; Wesson</last_name>");
    }

    @Test
    public void shouldGenerateXmlForPolymorphicChildren() throws IOException {
        deleteAndPopulateTables("articles", "comments", "tags");
        Article a = Article.findFirst("title = ?", "ActiveJDBC polymorphic associations");
        a.add(Comment.create("author", "igor", "content", "this is just a test comment text"));
        a.add(Tag.create("content", "orm"));
        LazyList<Article> articles = Article.where("title = ?", "ActiveJDBC polymorphic associations")
                .include(Tag.class, Comment.class);

        String xml = articles.toXml(true, true);
        XPathHelper h = new XPathHelper(xml);
        a(h.count("/articles/article")).shouldEqual(1);
        a(h.count("/articles/article/comments/comment")).shouldEqual(1);
        a(h.count("/articles/article/tags/tag")).shouldEqual(1);
        the(h.selectText("/articles/article[1]/comments/comment[1]/content")).shouldBeEqual(
                "this is just a test comment text");
        the(h.selectText("/articles/article[1]/tags/tag[1]/content")).shouldBeEqual("orm");
    }

    @Test
    public void shouldKeepParametersCase() {
        Person p = Person.create("name", "Joe", "last_name", "Schmoe");

        String xml = p.toXml(true, true);
        the(xml).shouldContain("<name>Joe</name>");
        the(xml).shouldContain("<last_name>Schmoe</last_name>");

        xml = p.toXml(true, true, "Name", "Last_Name");
        the(xml).shouldContain("<Name>Joe</Name>");
        the(xml).shouldContain("<Last_Name>Schmoe</Last_Name>");
    }
}
