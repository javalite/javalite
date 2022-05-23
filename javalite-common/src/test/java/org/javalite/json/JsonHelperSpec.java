package org.javalite.json;

import org.javalite.common.Util;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.map;
import static org.javalite.json.JSONHelper.toJsonString;
import static org.javalite.json.JSONHelper.toMap;
import static org.javalite.test.jspec.JSpec.*;

/**
 * @author Igor Polevoy on 5/26/16.
 */
public class JsonHelperSpec {

    @Test
    public void shouldConvertObject2JSON() {
        class Person {
            String firstName, lastName;
            public Person(String firstName, String lastName) {
                this.firstName = firstName;
                this.lastName = lastName;
            }

            public String getFirstName() {
                return firstName;
            }

            public String getLastName() {
                return lastName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }
        }
        a(toJsonString(new Person("John", "Smith"))).shouldBeEqual("{\"firstName\":\"John\",\"lastName\":\"Smith\"}");
    }

    @Test
    public void shouldConvertObject2JSONPrettyPrintString() {
        Map m = toMap("{ \"firstName\" : \"John\",\"lastName\" : \"Smith\", \"age\": 22, \"1\": 1 }");
        String pretty = toJsonString(m, true);
        a(pretty).shouldBeEqual("{" + System.lineSeparator() +
                "  \"1\" : 1," + System.lineSeparator() +
                "  \"age\" : 22," + System.lineSeparator() +
                "  \"firstName\" : \"John\"," + System.lineSeparator() +
                "  \"lastName\" : \"Smith\"" + System.lineSeparator() +
                "}"
        );
    }

    @Test
    public void shouldConvertArray2List() {
        List l = JSONHelper.toList("[1, 2]");
        $(l.size()).shouldBeEqual(2);
        $(l.get(0)).shouldBeEqual(1);
        $(l.get(1)).shouldBeEqual(2);
    }

    @Test
    public void shouldConvertMap2Map() {
        Map m = toMap("{ \"name\" : \"John\", \"age\": 22 }");
        $(m.size()).shouldBeEqual(2);
        $(m.get("name")).shouldBeEqual("John");
        $(m.get("age")).shouldBeEqual(22);
    }

    @Test
    public void shouldConvertMaps2Maps() {
        Map[] maps = JSONHelper.toMaps("[{ \"name\" : \"John\", \"age\": 22 },{ \"name\" : \"Samantha\", \"age\": 21 }]");
        $(maps.length).shouldBeEqual(2);
        $(maps[0].get("name")).shouldBeEqual("John");
        $(maps[0].get("age")).shouldBeEqual(22);
        $(maps[1].get("name")).shouldBeEqual("Samantha");
        $(maps[1].get("age")).shouldBeEqual(21);
    }

    @Test
    public void shouldSanitizeString() {
        String result = JSONHelper.sanitize("\thello");
        the(result).shouldBeEqual("\\thello");
    }

    @Test
    public void shouldEscapeTab() {
        String result = JSONHelper.escapeControlChars("\tThomas Jefferson University");
        the(result).shouldBeEqual("\\tThomas Jefferson University");
    }

    @Test
    public void shouldCleanTab() {
        String result = JSONHelper.sanitize("\tThomas Jefferson University - Employer Match Processing - Action Required", true);
        a(result).shouldBeEqual("Thomas Jefferson University - Employer Match Processing - Action Required");
    }

    @Test
    public void shouldEscapeNewLine() {
        String result = JSONHelper.escapeControlChars("line 1" + System.getProperty("line.separator") + "line 2");
        if(System.getProperty("os.name").contains("indows")){
            the(result).shouldBeEqual("line 1\\r\\nline 2");
        }else{
            the(result).shouldBeEqual("line 1\\nline 2");
        }
    }

    @Test
    public void shouldCleanNewLine() {
        String result = JSONHelper.cleanControlChars("line 1" + System.getProperty("line.separator") + "line 2");
        the(result).shouldBeEqual("line 1line 2");
    }

    @Test
    public void shouldCleanSelectedChars() {
        String result;
        if(System.getProperty("os.name").contains("indows")){
            result = JSONHelper.sanitize("line 1" + System.getProperty("line.separator") + "\tline 2", true, '\n', '\r');
        }else {
            result = JSONHelper.sanitize("line 1" + System.getProperty("line.separator") + "\tline 2", true, '\n');
        }
        the(result).shouldBeEqual("line 1\tline 2");
    }

    @Test
    public void shouldConvertToJsonObject() {

        try {
            JSONHelper.toJsonObject("name");
        } catch (Exception exception) {
            the(exception).shouldBeType(IllegalArgumentException.class);
        }

        String result = JSONHelper.toJsonObject("name", "Joe", "age", 23, "dob", new Date());
        Map mapResult = toMap(result);
        the(mapResult.get("name")).shouldBeEqual("Joe");
        the(mapResult.get("age")).shouldBeEqual(23);
        the(mapResult.get("dob")).shouldNotBeNull();
    }

    @Test
    public void shouldConvertNull() {
        String json = JSONHelper.toJsonObject("null", null);
        the(toMap(json).get("null")).shouldBeNull();
    }

    @Test
    public void shouldEscapeDoubleQuote() {
        String quoted = Util.readResource("/double_quote.txt");
        String json = JSONHelper.sanitize(quoted);
        the(json).shouldBeEqual("text with double \\\" quote");
        List l = JSONHelper.toList("[\"" + json + "\"]");
        the(l.get(0)).shouldBeEqual("text with double \" quote");
    }

    @Test
    public void shouldParseJsonToMap() {
        Map map= toMap(Util.readResource("/john.json"));
        the(map.get("first_name")).shouldBeEqual("John");
        the(map.get("last_name")).shouldBeEqual("Doe");
    }

    @Test
    public void shouldParseJsonToMaps() {
        Map[] people= JSONHelper.toMaps(Util.readResource("/people.json"));
        the(people[0].get("first_name")).shouldBeEqual("John");
        the(people[1].get("first_name")).shouldBeEqual("Jane");
    }

    @Test
    public void shouldParseNull() {
        Map person= toMap(Util.readResource("/contains_null.json"));
        the(person.get("name")).shouldBeEqual("John");
        the(person.get("adult")).shouldBeNull();
    }

    @Test
    public void shouldConvertWithNull() {
        Map map = map("name", "John", "married", null);

        String json = JSONHelper.toJsonString(map);
        the(json).shouldContain("\"name\":\"John\"");
        the(json).shouldContain("\"married\":null");
    }
    
    record Human(String firstName, String lastName){}
    
    @Test
    public void shouldSerializeJavaRecord(){
        Map hm = toMap(toJsonString(new Human("Joe", "Shmoe")));
        the(hm.get("firstName")).shouldBeEqual("Joe");
        the(hm.get("lastName")).shouldBeEqual("Shmoe");
    }

    @Test
    public void shouldGenerateJSONObjectFromPairs(){

        String person = toJsonString("first_name", "Marilyn", "last_name", "Monroe");
        Map personMap = JSONHelper.toJSONMap(person);

        the(personMap.get("first_name")).shouldBeEqual("Marilyn");
        the(personMap.get("last_name")).shouldBeEqual("Monroe");

    }
}



