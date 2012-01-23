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

package org.javalite.activejdbc;


import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class ToJsonSpec extends ActiveJDBCTest {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldGenerateSimpleJson() throws IOException {
        deleteAndPopulateTable("people");
        Person p  = (Person)Person.findById(1);
        //test no indent
        String json = p.toJson(false, "name", "last_name", "dob");
        mapper.readTree(json);//check validity
        the(json).shouldBeEqual("{\"model_class\":\"org.javalite.activejdbc.test_models.Person\",\"dob\":\"1934-12-01\",\"last_name\":\"Smith\",\"name\":\"John\"}");
        //test indent
        json = p.toJson(true, "name", "last_name", "dob");
        mapper.readTree(json);//check validity
        String expected = "{\n" +
                "  \"model_class\":\"org.javalite.activejdbc.test_models.Person\",\n" +
                "  \"dob\":\"1934-12-01\",\n" +
                "  \"last_name\":\"Smith\",\n" +
                "  \"name\":\"John\"\n" +
                "}";
        the(json).shouldBeEqual(expected);
    }

    @Test
    public void shouldIncludePrettyChildren() throws IOException {
        deleteAndPopulateTables("users", "addresses");
        List<User> personList = User.findAll().orderBy("id").include(Address.class);
        User u = personList.get(0);
        String json = u.toJson(true);
        mapper.readTree(json);//check validity
        a(json).shouldEqual("{\n" +
                "  \"model_class\":\"org.javalite.activejdbc.test_models.User\",\n" +
                "  \"email\":\"mmonroe@yahoo.com\",\n" +
                "  \"last_name\":\"Monroe\",\n" +
                "  \"first_name\":\"Marilyn\",\n" +
                "  \"id\":\"1\",\n" +
                "  \"children\" : {\n" +
                "    \"addresses\" : [\n" +
                "    {\n" +
                "      \"model_class\":\"org.javalite.activejdbc.test_models.Address\",\n" +
                "      \"user_id\":\"1\",\n" +
                "      \"address2\":\"apt 31\",\n" +
                "      \"state\":\"IL\",\n" +
                "      \"address1\":\"123 Pine St.\",\n" +
                "      \"zip\":\"60606\",\n" +
                "      \"city\":\"Springfield\",\n" +
                "      \"id\":\"1\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"model_class\":\"org.javalite.activejdbc.test_models.Address\",\n" +
                "      \"user_id\":\"1\",\n" +
                "      \"address2\":\"apt 21\",\n" +
                "      \"state\":\"IL\",\n" +
                "      \"address1\":\"456 Brook St.\",\n" +
                "      \"zip\":\"60606\",\n" +
                "      \"city\":\"Springfield\",\n" +
                "      \"id\":\"2\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"model_class\":\"org.javalite.activejdbc.test_models.Address\",\n" +
                "      \"user_id\":\"1\",\n" +
                "      \"address2\":\"apt 32\",\n" +
                "      \"state\":\"IL\",\n" +
                "      \"address1\":\"23 Grove St.\",\n" +
                "      \"zip\":\"60606\",\n" +
                "      \"city\":\"Springfield\",\n" +
                "      \"id\":\"3\"\n" +
                "    }\n" +
                "]\n" +
                "}\n" +
                "}");
    }

    @Test
    public void shouldIncludeUglyChildren() throws IOException {
        deleteAndPopulateTables("users", "addresses");
        List<User> personList = User.findAll().orderBy("id").include(Address.class);
        User u = personList.get(0);
        String json = u.toJson(false);
        mapper.readTree(json);//check validity
        a(json).shouldEqual("{\"model_class\":\"org.javalite.activejdbc.test_models.User\",\"email\":\"mmonroe@yahoo.com\",\"last_name\":\"Monroe\",\"first_name\":\"Marilyn\",\"id\":\"1\",\"children\" : {\"addresses\" : [{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"1\",\"address2\":\"apt 31\",\"state\":\"IL\",\"address1\":\"123 Pine St.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"1\"},{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"1\",\"address2\":\"apt 21\",\"state\":\"IL\",\"address1\":\"456 Brook St.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"2\"},{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"1\",\"address2\":\"apt 32\",\"state\":\"IL\",\"address1\":\"23 Grove St.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"3\"}]}}");
    }

    @Test
    public void shouldIncludeOnlyProvidedAttributes() throws IOException {
        deleteAndPopulateTables("users", "addresses");

        User u = (User)User.findById(1);
        String json = u.toJson(true, "email", "last_name");
        mapper.readTree(json);//check validity
        the(json).shouldBeEqual("{\n" +
                "  \"model_class\":\"org.javalite.activejdbc.test_models.User\",\n" +
                "  \"email\":\"mmonroe@yahoo.com\",\n" +
                "  \"last_name\":\"Monroe\"\n" +
                "}");
    }

    @Test
    public void shouldGenerateFromList() throws IOException {
        deleteAndPopulateTables("users", "addresses");
        LazyList<User> personList = User.findAll().orderBy("id").include(Address.class);

        String json = personList.toJson(false);
        mapper.readTree(json);//check validity
        a(json).shouldBeEqual("[{\"model_class\":\"org.javalite.activejdbc.test_models.User\",\"email\":\"mmonroe@yahoo.com\",\"last_name\":\"Monroe\",\"first_name\":\"Marilyn\",\"id\":\"1\",\"children\" : {\"addresses\" : [{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"1\",\"address2\":\"apt 31\",\"state\":\"IL\",\"address1\":\"123 Pine St.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"1\"},{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"1\",\"address2\":\"apt 21\",\"state\":\"IL\",\"address1\":\"456 Brook St.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"2\"},{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"1\",\"address2\":\"apt 32\",\"state\":\"IL\",\"address1\":\"23 Grove St.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"3\"}]}},{\"model_class\":\"org.javalite.activejdbc.test_models.User\",\"email\":\"jdoe@gmail.com\",\"last_name\":\"Doe\",\"first_name\":\"John\",\"id\":\"2\",\"children\" : {\"addresses\" : [{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"2\",\"address2\":\"apt 34\",\"state\":\"IL\",\"address1\":\"143 Madison St.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"4\"},{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"2\",\"address2\":\"apt 35\",\"state\":\"IL\",\"address1\":\"153 Creek St.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"5\"},{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"2\",\"address2\":\"apt 36\",\"state\":\"IL\",\"address1\":\"163 Gorge St.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"6\"},{\"model_class\":\"org.javalite.activejdbc.test_models.Address\",\"user_id\":\"2\",\"address2\":\"apt 37\",\"state\":\"IL\",\"address1\":\"173 Far Side.\",\"zip\":\"60606\",\"city\":\"Springfield\",\"id\":\"7\"}]}}]");
    }

    @Test
    public void shouldEscapeDoubleQuote() throws IOException {
        Page p = new Page();
        p.set("description", "bad \" description");
        JsonNode node = mapper.readTree(p.toJson(true));
        a(node.get("description").toString()).shouldBeEqual("\"bad \\\" description\"");

        //ensure no NPE:
        p = new Page();
        p.set("description", null);
        p.toJson(true);
    }


    @Test
    public void shouldInjectCustomContentIntoJson() throws IOException {
        deleteAndPopulateTable("posts");

        Post p = (Post)Post.findById(1);
        String json = p.toJson(true, "title");

        Map map = mapper.readValue(json, Map.class);
        Map injected = (Map) map.get("injected");
        a(injected.get("secret_name")).shouldBeEqual("Secret Name");
    }
}

