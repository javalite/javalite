package org.javalite.common;

import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.map;
import static org.javalite.common.JsonHelper.toJsonString;
import static org.javalite.test.jspec.JSpec.$;
import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

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
    public void shouldConvertArray2List() {
        List l = JsonHelper.toList("[1, 2]");
        $(l.size()).shouldBeEqual(2);
        $(l.get(0)).shouldBeEqual(1);
        $(l.get(1)).shouldBeEqual(2);
    }

    @Test
    public void shouldConvertMap2Map() {
        Map m = JsonHelper.toMap("{ \"name\" : \"John\", \"age\": 22 }");
        $(m.size()).shouldBeEqual(2);
        $(m.get("name")).shouldBeEqual("John");
        $(m.get("age")).shouldBeEqual(22);
    }

    @Test
    public void shouldConvertMaps2Maps() {
        Map[] maps = JsonHelper.toMaps("[{ \"name\" : \"John\", \"age\": 22 },{ \"name\" : \"Samantha\", \"age\": 21 }]");
        $(maps.length).shouldBeEqual(2);
        $(maps[0].get("name")).shouldBeEqual("John");
        $(maps[0].get("age")).shouldBeEqual(22);
        $(maps[1].get("name")).shouldBeEqual("Samantha");
        $(maps[1].get("age")).shouldBeEqual(21);
    }

    @Test
    public void shouldSanitizeString() {
        String result = JsonHelper.sanitize("\thello");
        the(result).shouldBeEqual("\\thello");
    }

    @Test
    public void shouldEscapeTab() {
        String result = JsonHelper.escapeControlChars("\tThomas Jefferson University");
        the(result).shouldBeEqual("\\tThomas Jefferson University");
    }

    @Test
    public void shouldCleanTab() {
        String result = JsonHelper.sanitize("\tThomas Jefferson University - Employer Match Processing - Action Required", true);
        a(result).shouldBeEqual("Thomas Jefferson University - Employer Match Processing - Action Required");
    }

    @Test
    public void shouldEscapeNewLine() {
        String result = JsonHelper.escapeControlChars("line 1" + System.getProperty("line.separator") + "line 2");
        the(result).shouldBeEqual("line 1\\nline 2");
    }

    @Test
    public void shouldCleanNewLine() {
        String result = JsonHelper.cleanControlChars("line 1" + System.getProperty("line.separator") + "line 2");
        the(result).shouldBeEqual("line 1line 2");
    }

    @Test
    public void shouldCleanSelectedChars() {
        String result = JsonHelper.sanitize("line 1" + System.getProperty("line.separator") + "\tline 2", true, '\n');
        the(result).shouldBeEqual("line 1\tline 2");
    }

    @Test
    public void shouldConvertToJsonObject() {

        try {
            JsonHelper.toJsonObject("name");
        } catch (Exception exception) {
            the(exception).shouldBeType(IllegalArgumentException.class);
        }

        String result = JsonHelper.toJsonObject("name", "Joe", "age", 23, "dob", new Date());
        Map mapResult = JsonHelper.toMap(result);
        the(mapResult.get("name")).shouldBeEqual("Joe");
        the(mapResult.get("age")).shouldBeEqual(23);
        the(mapResult.get("dob")).shouldNotBeNull();
    }

    @Test
    public void shouldConvertNull() {
        String json = JsonHelper.toJsonObject("null", null);
        the(JsonHelper.toMap(json).get("null")).shouldBeNull();
    }

    @Test
    public void shouldEscapeDoubleQuote() {
        String quoted = Util.readResource("/double_quote.txt");
        String json = JsonHelper.sanitize(quoted);
        the(json).shouldBeEqual("text with double \\\" quote");
        List l = JsonHelper.toList("[\"" + json + "\"]");
        the(l.get(0)).shouldBeEqual("text with double \" quote");
    }
}


