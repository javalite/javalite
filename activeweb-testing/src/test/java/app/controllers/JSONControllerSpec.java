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
}
