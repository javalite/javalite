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

    String jsonMessage = """
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


    JSONMap universityMap = toMap(university);

    @Test
    public void shouldGetList(){
        String jsonString = """            
                {
                   "university": {
                        "students" : ["mary", "joe"]
                    }
                }
                """;

        JSONBase json = new JSONBase(jsonString);

        the(json.get("university.students")).shouldBeA(List.class);
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
        JSONBase jsonBase = new JSONBase(JSONHelper.toMap(jsonMessage));
        the(jsonBase.get("message_body")).shouldBeEqual("Test message");
    }

    @Test
    public void shouldFindTypes(){

        String json  = """
            {
              "university": {
                   "roles":{
                        "role1": "secretary",
                        "role2": "lecturer",
                        "role3": "janitor"
                   },
                  "students":[
                      {"first_name": "Mary", "role": "student"},
                      {"first_name": "Joe"}
                  ],
                  "faculty":[
                      {"first_name": "Mike", "role": "professor"},
                      {"first_name": "Judith", "role": "professor"}
                  ]
              }
            }""";

        JSONBase jsonBase = new JSONBase(json);
        the(jsonBase.get("university.roles")).shouldBeA(Map.class);
        the(jsonBase.get("university.students")).shouldBeA(List.class);
        the(jsonBase.get("university.faculty")).shouldBeA(List.class);
    }

    String university2  = """
            {
              "university": {
                   "properties": {
                        "accredited":true,
                        "private":false
                   },
                   "roles":{
                        "role1": "secretary",
                        "role2": "lecturer",
                        "role3": "janitor"
                   },
                  "students":[
                      {"first_name": "Mary", "role": "student"},
                      {"first_name": "Joe"}
                  ],
                  "faculty":[
                      {"first_name": "Mike", "role": "professor"},
                      {"first_name": "Judith", "role": "professor"}
                  ]
              }
            }""";


    @Test
    public void shouldValidateList(){
        JSONBase university = new JSONBase(university2);
        university.validateList("university.students");
        the(university).shouldBe("valid");
    }

    @Test
    public void shouldInValidateList(){
        JSONBase university = new JSONBase(university2);
        university.validateList("university.roles");
        the(university).shouldNotBe("valid");
        the(university.errors().get("university.roles")).shouldEqual("value under path 'university.roles' is not an array");
    }

    @Test
    public void shouldValidateMap(){
        JSONBase university = new JSONBase(university2);
        university.validateMap("university.roles");// this is a list, not a map
        the(university).shouldBe("valid");
    }

    @Test
    public void shouldInValidateMap(){

        JSONBase university = new JSONBase(university2);
        university.validateMap("university.students");// this is a list, not a map
        the(university).shouldNotBe("valid");
        the(university.errors().get("university.students")).shouldEqual("value under path 'university.students' is not an object");
    }

    @Test
    public void shouldValidateBoolean(){
        JSONBase university = new JSONBase(university2);
        university.validateBoolean("university.properties.accredited");
        the(university).shouldBe("valid");
    }

    @Test
    public void shouldInValidateBoolean(){
        JSONBase university = new JSONBase(university2);
        university.validateBoolean("university.properties.accredited", false);

        the(university).shouldNotBe("valid");
        the(university.errors().get("university.properties.accredited")).shouldEqual("value under path 'university.properties.accredited' is not false");
    }

    @Test
    public void shouldInValidateNonBoolean(){

        JSONBase university = new JSONBase(university2);
        university.validateBoolean("university.roles.role1");

        the(university).shouldNotBe("valid");
        the(university.errors().get("university.roles.role1")).shouldEqual("value under path 'university.roles.role1' is not true");
    }
}
