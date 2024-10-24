package app.controllers;

import org.javalite.activeweb.ControllerSpec;
import org.javalite.json.JSONList;
import org.javalite.json.JSONMap;
import org.junit.Test;

public class JSONControllerSpec extends ControllerSpec {

    @Test
    public void shouldPostJSONMap(){

        String doc = """
                {
                    "first_name" : "John"
                }
                """;
        request().json(doc).post("index1");

        JSONMap response = responseJSONMap();

        the(response).shouldContain("first_name");
        the(response).shouldContain("last_name");

        the(response.get("first_name")).shouldContain("John");
        the(response.get("last_name")).shouldContain("Doe");

        the(contentType()).shouldBeEqual("application/json");
    }

    @Test
    public void shouldPostJSONList(){

        String doc = """
                [
                    "John" , "Doe"
                ]
                """;
        request().json(doc).post("index2");

        JSONList response = responseJSONList();

        the(response).shouldContain("John");
        the(response).shouldContain("Doe");
        the(contentType()).shouldBeEqual("application/json");
    }

    @Test
    public void shouldPostJSONString(){

        String doc = """
                [
                    "John" , "Doe"
                ]
                """;
        request().json(doc).post("index2");

        JSONList response = responseJSONList();

        the(response).shouldContain("John");
        the(response).shouldContain("Doe");
        the(contentType()).shouldBeEqual("application/json");
    }

    @Test
    public void shouldConvertResponseToObject(){

        String doc = """
                {
                    "firstName" : "John",
                    "lastName" : "Deer"
                }
                """;
        request().json(doc).post("index3");

        the(responseObject(Person3.class).toString()).shouldBeEqual("Person3[firstName=John, lastName=Deer]");
        the(contentType()).shouldBeEqual("application/json");
    }

    @Test
    public void shouldConvertResponseToRecord(){

        Person3 person3 = new Person3("John", "Doe");
        request().json(person3).post("index3");

        the(responseObject(Person3.class)).shouldBeEqual(person3);
        the(contentType()).shouldBeEqual("application/json");
    }
}
