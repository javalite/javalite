package org.javalite.json;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.javalite.common.Util;
import org.junit.Test;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.javalite.common.Collections.map;
import static org.javalite.test.jspec.JSpec.*;

/**
 * @author Igor Polevoy on 5/26/16.
 */
public class JSONHelperSpec {

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
        a(JSONHelper.toJSON(new Person("John", "Smith"))).shouldBeEqual("{\"firstName\":\"John\",\"lastName\":\"Smith\"}");
    }

    @Test
    public void shouldConvertObject2JSONPrettyPrintString() {
        var m = JSONHelper.toMap("{ \"firstName\" : \"John\",\"lastName\" : \"Smith\", \"age\": 22, \"1\": 1 }");
        String pretty = JSONHelper.toJSON(m, true);
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
        var l = JSONHelper.toList("[1, 2]");
        $(l.size()).shouldBeEqual(2);
        $(l.get(0)).shouldBeEqual(1);
        $(l.get(1)).shouldBeEqual(2);
    }

    @Test
    public void shouldConvertMap2Map() {
        var m = JSONHelper.toMap("{ \"name\" : \"John\", \"age\": 22 }");
        $(m.size()).shouldBeEqual(2);
        $(m.get("name")).shouldBeEqual("John");
        $(m.get("age")).shouldBeEqual(22);
    }

    @Test
    public void shouldConvertMaps2Maps() {
        var list = JSONHelper.toList("[{ \"name\" : \"John\", \"age\": 22 },{ \"name\" : \"Samantha\", \"age\": 21 }]");
        var maps = list.getMaps();
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
        if (System.getProperty("os.name").contains("indows")) {
            the(result).shouldBeEqual("line 1\\r\\nline 2");
        } else {
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
        if (System.getProperty("os.name").contains("indows")) {
            result = JSONHelper.sanitize("line 1" + System.getProperty("line.separator") + "\tline 2", true, '\n', '\r');
        } else {
            result = JSONHelper.sanitize("line 1" + System.getProperty("line.separator") + "\tline 2", true, '\n');
        }
        the(result).shouldBeEqual("line 1\tline 2");
    }

    @Test
    public void shouldConvertToJsonObject() {

        try {
            JSONHelper.toJSON("name");
        } catch (Exception exception) {
            the(exception).shouldBeType(IllegalArgumentException.class);
        }

        String result = JSONHelper.toJSON("name", "Joe", "age", 23, "dob", new Date());
        var mapResult = JSONHelper.toMap(result);
        the(mapResult.get("name")).shouldBeEqual("Joe");
        the(mapResult.get("age")).shouldBeEqual(23);
        the(mapResult.get("dob")).shouldNotBeNull();
    }

    @Test
    public void shouldConvertNull() {
        String json = JSONHelper.toJSON("null", null);
        the(JSONHelper.toMap(json).get("null")).shouldBeNull();
    }

    @Test
    public void shouldEscapeDoubleQuote() {
        String quoted = Util.readResource("/double_quote.txt");
        String json = JSONHelper.sanitize(quoted);
        the(json).shouldBeEqual("text with double \\\" quote");
        var l = JSONHelper.toList("[\"" + json + "\"]");
        the(l.get(0)).shouldBeEqual("text with double \" quote");
    }

    @Test
    public void shouldParseJsonToMap() {
        var map = JSONHelper.toMap(Util.readResource("/john.json"));
        the(map.get("first_name")).shouldBeEqual("John");
        the(map.get("last_name")).shouldBeEqual("Doe");
    }

    @Test
    public void shouldParseJsonToMaps() {
        var people = JSONHelper.toList(Util.readResource("/people.json")).getMaps();
        the(people[0].get("first_name")).shouldBeEqual("John");
        the(people[1].get("first_name")).shouldBeEqual("Jane");
    }

    @Test
    public void shouldParseNull() {
        var person = JSONHelper.toMap(Util.readResource("/contains_null.json"));
        the(person.get("name")).shouldBeEqual("John");
        the(person.get("adult")).shouldBeNull();
    }

    @Test
    public void shouldConvertWithNull() {
        Map map = map("name", "John", "married", null);

        String json = JSONHelper.toJSON(map);
        the(json).shouldContain("\"name\":\"John\"");
        the(json).shouldContain("\"married\":null");
    }

    record Human(String firstName, String lastName) {
    }

    @Test
    public void shouldSerializeJavaRecord() {
        var hm = JSONHelper.toMap(JSONHelper.toJSON(new Human("Joe", "Shmoe")));
        the(hm.get("firstName")).shouldBeEqual("Joe");
        the(hm.get("lastName")).shouldBeEqual("Shmoe");
    }

    @Test
    public void shouldGenerateJSONObjectFromPairs() {

        String person = JSONHelper.toJSON("first_name", "Marilyn", "last_name", "Monroe");
        Map<String, Object> personMap = JSONHelper.toMap(person);

        the(personMap.get("first_name")).shouldBeEqual("Marilyn");
        the(personMap.get("last_name")).shouldBeEqual("Monroe");

    }


    static class Table {
        private int legCount;

        public Table(){}

        public Table(int legCount) {
            this.legCount = legCount;
        }

        public int getLegCount() {
            return legCount;
        }
    }

    @Test
    public void shouldConvertObjectToJSON() {
        Table t = new Table(3);
        String json = JSONHelper.toJSON(t);
        Map<String, Object> map = JSONHelper.toMap(json);
        the(map.get("legCount")).shouldEqual(3);
    }

    @Test
    public void shouldConvertJSONToObject() {


        String json = """
                { "legCount" : 4 }
                """;

        Table t  = JSONHelper.toObject(json, Table.class);

        the(t.legCount).shouldEqual(4);
    }


//    @JsonFilter("TestFilter")
    @JsonIncludeProperties()
    class TestObject {
        private Map<String, Object> map = new HashMap<>();
        private Map<String, Object> map2 = new HashMap<>();

        public TestObject() {

        }

        public Map<String, Object> getMap() {
            return map;
        }

        public void setMap(Map<String, Object> map) {
            this.map = map;
        }

    }

    class TestObject2 extends TestObject {

        private String type = this.getClass().getName();
        public String getType2() {
            return type;
        }

        public void setType2(String type) {
            this.type = type;
        }
    }

    @Test
    public void convertModelToJSON() throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        var filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter("TestFilter", SimpleBeanPropertyFilter.filterOutAllExcept("map2", "class.name"));
        objectMapper.setFilterProvider(filterProvider);

        System.out.println(objectMapper.writeValueAsString(new TestObject2()));
    }

}



