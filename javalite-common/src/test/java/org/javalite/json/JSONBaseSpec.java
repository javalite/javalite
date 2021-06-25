package org.javalite.json;

import org.javalite.validation.RangeValidator;
import org.junit.Test;


import java.util.*;

import static org.javalite.json.JSONHelper.toMap;
import static org.javalite.test.jspec.JSpec.$;
import static org.javalite.test.jspec.JSpec.the;

public class JSONBaseSpec {
    String university = """            
                {
                   "university": {
                        "students" : {
                            "mary": { "first_name": "Mary", "age": 35, "married": false},
                            "joe": { "first_name": "Joe"}
                        }
                    }
                }
                """;

    Map universityMap = toMap(university);

    @Test
    public void shouldGetList(){
        String jsonString = """            
                {
                   "university": {
                        "students" : ["mary", "joe"]
                    }
                }
                """;

        JSONBase json = new JSONBase(toMap(jsonString));

        JSONList list = json.getList("university.students");
        the(list.get(0)).shouldBeEqual("mary");
        the(list.get(1)).shouldBeEqual("joe");
    }

    @Test
    public void shouldGetMap(){

        JSONMap students = new JSONBase(universityMap).getMap("university.students");
        $(students.getMap("mary").get("first_name")).shouldBeEqual("Mary");
        $(students.getMap("joe").get("first_name")).shouldBeEqual("Joe");
    }

    @Test
    public void shouldGetDeepAttribute(){

        JSONBase json = new JSONBase(universityMap);
        the(json.get("university.students.mary.first_name")).shouldBeEqual("Mary");
        the(json.get("university.students.joe.first_name")).shouldBeEqual("Joe");
    }

    @Test
    public void shouldValidatePresenceOfAttribute(){
        class Students extends JSONBase {
            public Students(Map jsonMap) {
                super(jsonMap);
                validatePresenceOf("university.students.mary.first_name");
            }
        }
        Students students =  new Students(universityMap);
        the(students).shouldBe("valid");
        the(students.errors().size()).shouldBeEqual(0);
    }

    @Test
    public void shouldInValidateAbsenceOfAttribute(){
        class Students extends JSONBase {
            public Students(Map map) {
                super(map);
                validatePresenceOf("university.students.joe.age");
            }
        }
        Students students =  new Students(universityMap);
        the(students).shouldNotBe("valid");
        the(students.errors().size()).shouldBeEqual(1);
        the(students.errors().get("university.students.joe.age")).shouldBeEqual("value is missing");
    }

    @Test
    public void shouldValidateNumericality(){
        class Students extends JSONBase {
            public Students(Map json) {
                super(json);
                validateNumericalityOf("university.students.mary.age");
            }
        }
        Students students =  new Students(universityMap);
        the(students).shouldBe("valid");
    }

    @Test
    public void shouldValidateRange(){
        class Students extends JSONBase {
            public Students(Map jsonMap) {
                super(jsonMap);
                validateWith(new RangeValidator("university.students.mary.age", 10, 50));
            }
        }
        Students students =  new Students(universityMap);
        the(students).shouldBe("valid");
    }

    @Test
    public void shouldInValidateRange(){
        class Students extends JSONBase {
            public Students(Map jsonMap) {
                super(jsonMap);
                validateWith(new RangeValidator("university.students.mary.age", 10, 20));
            }
        }
        Students students =  new Students(universityMap);
        the(students).shouldNotBe("valid");

        the(students.errors().size()).shouldBeEqual(1);
        the(students.errors().get("university.students.mary.age")).shouldBeEqual("value should be within limits: > 10 and < 20");
    }

    @Test
    public void shouldReadImmediateChild(){

        String json  = """
            {
              "message_body": "Test message",
              "from": "2012220321",
              "to": [
                {
                  "phone": [
                    "+12015550123"
                  ]
                }
              ]
            }""";

        JSONBase jsonBase = new JSONBase(JSONHelper.toMap(json));
        the(jsonBase.get("message_body")).shouldBeEqual("Test message");
    }
}
