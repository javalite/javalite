package org.javalite.activeweb;

import freemarker.template.TemplateException;
import org.javalite.activeweb.mojo.Generator;
import org.javalite.json.JSONHelper;
import org.javalite.json.JSONMap;
import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.javalite.activeweb.mojo.PrintMojo.printEndpointDefinitions;
import static org.javalite.common.Util.readFile;
import static org.javalite.json.JSONHelper.toMap;
import static org.javalite.test.jspec.JSpec.$;
import static org.javalite.test.jspec.JSpec.the;

public class EndpointFinderSpec {

    @Test
    public void shouldFindCustomEndpointsFromClasspath() {

        SystemStreamUtil.replaceError();
        SystemStreamUtil.replaceOut();

        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig1", this.getClass().getClassLoader());
        endpointFinder.setApiLocation("src/test/open-api");
        List<EndPointDefinition> endPointDefinitions = endpointFinder.getCustomEndpointDefinitions(Format.JSON);
        printEndpointDefinitions("Title", endPointDefinitions);

        the(endPointDefinitions.size()).shouldBeEqual(5);

        $(exists(endPointDefinitions, "/hello", "app.controllers.TestController", "foo", "", new EndPointHttpMethod(HttpMethod.GET, null))).shouldBeTrue();
        $(exists(endPointDefinitions,  "/person_save", "app.controllers.CustomController", "savePerson", "app.controllers.Person", new EndPointHttpMethod(HttpMethod.POST, """
            {
              
              "summary": "Show API version details",
              "responses": {
                "200": {
                  "description": "200 response"
                }
                         
              }
            }""".replaceAll("([\\r\\n])", "")))).shouldBeTrue();
        $(exists(endPointDefinitions, "/segments/{id}", "app.controllers.SegmentsController", "index", "", new EndPointHttpMethod(HttpMethod.GET, null))).shouldBeTrue();

        $(exists(endPointDefinitions, "/about", "app.controllers.HomeController", "about", "",  new EndPointHttpMethod(HttpMethod.GET, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/solutions", "app.controllers.HomeController", "solutions", "", new EndPointHttpMethod(HttpMethod.GET, null))).shouldBeTrue();

        the(SystemStreamUtil.getSystemErr()).shouldContain("WARNING: Failed to find a method for controller: 'class app.controllers.HomeController' and action: 'team'. Check your RouteConfig class.");

        the(SystemStreamUtil.getSystemOut()).shouldContain("""
                +--------+----------------+--------------+------------------------------------+------------------------------------+
                | Number | Path           | HTTP Methods | Controller                         | Method                             |
                +--------+----------------+--------------+------------------------------------+------------------------------------+
                | 1      | /hello         | GET          | app.controllers.TestController     | foo()                              |
                | 2      | /person_save   | POST         | app.controllers.CustomController   | savePerson(app.controllers.Person) |
                | 3      | /segments/{id} | GET          | app.controllers.SegmentsController | index()                            |
                | 4      | /about         | GET          | app.controllers.HomeController     | about()                            |
                | 5      | /solutions     | GET          | app.controllers.HomeController     | solutions()                        |
                +--------+----------------+--------------+------------------------------------+------------------------------------+""");

        SystemStreamUtil.restoreSystemErr();
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldFindStandardEndpointsFromClasspath() {

        SystemStreamUtil.replaceOut();

        EndpointFinder endpointFinder = new EndpointFinder(this.getClass().getClassLoader());
        endpointFinder.setApiLocation("src/test/open-api");
        List<EndPointDefinition> endPointDefinitions = endpointFinder.getStandardEndpointDefinitions(Format.JSON);

        printEndpointDefinitions("Title", endPointDefinitions);
        the(endPointDefinitions.size()).shouldBeEqual(23);

        //true below

        $(exists(endPointDefinitions, "/test/index", "app.controllers.TestController", "index", "",
                new EndPointHttpMethod(HttpMethod.GET, null),
                new EndPointHttpMethod(HttpMethod.POST, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/test/foo", "app.controllers.TestController", "foo", "", new EndPointHttpMethod(HttpMethod.GET, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/test/save_person", "app.controllers.TestController", "savePerson", "app.controllers.Person", new EndPointHttpMethod(HttpMethod.POST, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/test/bar", "app.controllers.TestController", "bar", "", new EndPointHttpMethod(HttpMethod.GET, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/http_methods/index", "app.controllers.HttpMethodsController", "index", "",
                new EndPointHttpMethod(HttpMethod.GET, null),
                new EndPointHttpMethod(HttpMethod.POST, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/http_methods/do_post", "app.controllers.HttpMethodsController", "doPost", "", new EndPointHttpMethod(HttpMethod.POST, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/http_methods/do_put", "app.controllers.HttpMethodsController", "doPut", "",
                new EndPointHttpMethod(HttpMethod.PUT,  """
                                                        {
                                                        "description" : "docs for doPut",
                                                        "responses": {
                                                                  "200": {
                                                                    "description": "200 put"
                                                                  }
                                                          }
                                                        }         
                                                        """.replaceAll("([\\r\\n])", "")))).shouldBeTrue();
        $(exists(endPointDefinitions, "/http_methods/do_options", "app.controllers.HttpMethodsController", "doOptions", "", new EndPointHttpMethod(HttpMethod.OPTIONS, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/http_methods/do_patch", "app.controllers.HttpMethodsController", "doPatch", "", new EndPointHttpMethod(HttpMethod.PATCH, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/http_methods/do_head", "app.controllers.HttpMethodsController", "doHead", "",
                new EndPointHttpMethod(HttpMethod.HEAD,"""
                                                        {
                                                        "description" : "docs for doHead",
                                                        "responses": {
                                                                  "200": {
                                                                    "description": "200 head"
                                                                  }
                                                          }       
                                                        }
                                                        """.replaceAll("([\\r\\n])", "")))).shouldBeTrue();
        $(exists(endPointDefinitions, "/segments/index", "app.controllers.SegmentsController", "index", "", new EndPointHttpMethod(HttpMethod.GET, null))).shouldBeTrue();
        $(exists(endPointDefinitions, "/custom/index", "app.controllers.CustomController", "index", "",

                new EndPointHttpMethod(HttpMethod.GET, readFile("src/test/open-api/app.controllers.CustomController#index-get.json").replaceAll("([\\r\\n])", "")))
        ).shouldBeTrue();
        $(exists(endPointDefinitions, "/custom/save_person", "app.controllers.CustomController", "savePerson", "app.controllers.Person",
                new EndPointHttpMethod(HttpMethod.POST, """
            {
              
              "summary": "Show API version details",
              "responses": {
                "200": {
                  "description": "200 response"
                }
                         
              }
            }""".replaceAll("([\\r\\n])", "")))).shouldBeTrue();

//        //false below
        $(exists(endPointDefinitions,  "/bad/get_age", "app.controllers.CustomController", "", "", new EndPointHttpMethod(HttpMethod.GET, ""))).shouldBeFalse();
        $(exists(endPointDefinitions, "/bad/foo", "app.controllers.CustomController", "", "", new EndPointHttpMethod(HttpMethod.GET, ""))).shouldBeFalse();
        $(exists(endPointDefinitions, "/bad/bar", "app.controllers.CustomController", "", "", new EndPointHttpMethod(HttpMethod.POST, ""))).shouldBeFalse();

//        //false below
        $(exists(endPointDefinitions, "/abstract/bar", "app.controllers.AbstractController", "", "", new EndPointHttpMethod(HttpMethod.GET, ""))).shouldBeFalse();
        $(exists(endPointDefinitions, "/bad/foo", "app.controllers.CustomController", "", "", new EndPointHttpMethod(HttpMethod.GET, ""))).shouldBeFalse();

        the(SystemStreamUtil.getSystemOut()).shouldContain("""
                +--------+--------------------------+--------------+---------------------------------------+------------------------------------+
                | Number | Path                     | HTTP Methods | Controller                            | Method                             |
                +--------+--------------------------+--------------+---------------------------------------+------------------------------------+
                | 1      | /custom/index            | GET,POST     | app.controllers.CustomController      | index()                            |
                | 2      | /custom/save_person      | POST         | app.controllers.CustomController      | savePerson(app.controllers.Person) |
                | 3      | /home/about              | GET          | app.controllers.HomeController        | about()                            |
                | 4      | /home/solutions          | GET          | app.controllers.HomeController        | solutions()                        |
                | 5      | /http_methods/index      | GET,POST     | app.controllers.HttpMethodsController | index()                            |
                | 6      | /http_methods/do_post    | POST         | app.controllers.HttpMethodsController | doPost()                           |
                | 7      | /http_methods/do_put     | PUT          | app.controllers.HttpMethodsController | doPut()                            |
                | 8      | /http_methods/do_head    | HEAD         | app.controllers.HttpMethodsController | doHead()                           |
                | 9      | /http_methods/do_options | OPTIONS      | app.controllers.HttpMethodsController | doOptions()                        |
                | 10     | /http_methods/do_patch   | PATCH        | app.controllers.HttpMethodsController | doPatch()                          |
                | 11     | /pet/add                 | POST         | app.controllers.PetController         | add()                              |
                | 12     | /pet/update              | PUT          | app.controllers.PetController         | update()                           |
                | 13     | /pet/find_by_status      | GET          | app.controllers.PetController         | findByStatus()                     |
                | 14     | /pet/get_pet             | GET          | app.controllers.PetController         | getPet()                           |
                | 15     | /pet/update_pet          | POST         | app.controllers.PetController         | updatePet()                        |
                | 16     | /pet/delete_pet          | DELETE       | app.controllers.PetController         | deletePet()                        |
                | 17     | /segments/index          | GET          | app.controllers.SegmentsController    | index()                            |
                | 18     | /segments/foobar         | GET          | app.controllers.SegmentsController    | foobar()                           |
                | 19     | /segments/foobar_2       | GET          | app.controllers.SegmentsController    | foobar2()                          |
                | 20     | /test/index              | GET,POST     | app.controllers.TestController        | index()                            |
                | 21     | /test/foo                | GET          | app.controllers.TestController        | foo()                              |
                | 22     | /test/save_person        | POST         | app.controllers.TestController        | savePerson(app.controllers.Person) |
                | 23     | /test/bar                | GET          | app.controllers.TestController        | bar()                              |
                +--------+--------------------------+--------------+---------------------------------------+------------------------------------+
                """);

        //we still get custom defined  in a default RouteConfig
        List<EndPointDefinition> customDefinitions =  endpointFinder.getCustomEndpointDefinitions(Format.JSON);
        the(customDefinitions.size()).shouldBeEqual(2);
        $(exists(customDefinitions, "/hello", "app.controllers.TestController", "foo", "", new EndPointHttpMethod(HttpMethod.GET, null))).shouldBeTrue();
        $(exists(customDefinitions, "/person_save", "app.controllers.CustomController", "savePerson", "app.controllers.Person", new EndPointHttpMethod(HttpMethod.POST, """
            {
              
              "summary": "Show API version details",
              "responses": {
                "200": {
                  "description": "200 response"
                }
                         
              }
            }""".replaceAll("([\\r\\n])", "")))).shouldBeTrue();

        SystemStreamUtil.restoreSystemOut();
    }

    /**
     * Validates that the collection endPointDefinitions contains an entry corresponding to the rest of arguments
     */
    private boolean exists(List<EndPointDefinition> endPointDefinitions, String path, String controllerName, String actionMethodName, String argumentTypeName, EndPointHttpMethod... endpointHttpMethods) {

        for (EndPointDefinition endPointDefinition : endPointDefinitions) {
            if (endPointDefinition.getPath().equals(path)
                    && endPointDefinition.getControllerClassName().equals(controllerName)
                    && endPointDefinition.getActionMethodName().equals(actionMethodName)
                    && endPointDefinition.getArgumentClassName().equals(argumentTypeName)
                    && endPointDefinition.contains(endpointHttpMethods)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void should_not_find_standard_endpoints_with_strictMode() {

        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig1", this.getClass().getClassLoader());
        List<EndPointDefinition> endPointDefinitions = endpointFinder.getStandardEndpointDefinitions(Format.JSON);
        the(endPointDefinitions.size()).shouldBeEqual(0);
    }

    //
    @Test
    public void shouldCollectOpenAPIDocs() {

        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig", this.getClass().getClassLoader());


        String baseTemplate = """
                {
                      "openapi": "3.0.0",
                      "info": {
                        "title": "Simple API overview",
                        "version": "3.0.0"
                      },
                      "paths":{
                      
                      }
                  }""";

        String formattedJSONString = endpointFinder.getOpenAPIDocs(baseTemplate, Format.JSON);

        Map apiMap= toMap(formattedJSONString);
        Map paths = (Map) apiMap.get("paths");
        the(paths.keySet().size()).shouldBeEqual(25);
        the(paths).shouldContain("/custom/index");
        the(paths).shouldContain("/custom/save_person");
        the(paths).shouldContain("/http_methods/do_head");
        the(paths).shouldContain("/http_methods/do_put");
        the(paths).shouldContain("/person_save");
        the(paths).shouldContain("/segments/foobar_2");
    }

    @Test
    public void shouldLoadAPIFromFile() {
        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig2", this.getClass().getClassLoader());

        endpointFinder.setApiLocation("src/test/open-api");
        List<EndPointDefinition> endPointDefinitions = endpointFinder.getCustomEndpointDefinitions(Format.JSON);

        the(endPointDefinitions.size()).shouldBeEqual(1);

        the(endPointDefinitions.get(0).getHTTPMethods().size()).shouldBeEqual(1);
        the(endPointDefinitions.get(0).hasOpenAPI()).shouldBeTrue();
    }


    @Test
    public void shouldFailDueToDuplicateDocs(){
         try{
             EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig3", this.getClass().getClassLoader());
             endpointFinder.setApiLocation("src/test/open-api");
             endpointFinder.getCustomEndpointDefinitions(Format.JSON);
         }catch(OpenAPIException exception){
             the(exception).shouldContain("The action: app.controllers.SegmentsController#foobar2 contains the OpenAPI documentation in a corresponding file, as well as in the annotation GET. Only one place of record is allowed.");
         }
    }

    @Test
    public void shouldFailDueToMalformedJSON(){

        try{
            EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig4", this.getClass().getClassLoader());
            endpointFinder.setApiLocation("src/test/open-api-malformed");
            endpointFinder.getCustomEndpointDefinitions(Format.JSON);
        }catch(OpenAPIException exception){
            the(exception.getMessage()).shouldBeEqual("Failed to parse a JSON object from file: 'src/test/open-api-malformed/app.controllers.SegmentsController#foobar-get.json' " +
                    "for controller: 'class app.controllers.SegmentsController' and action method: 'foobar'");
        }
    }

    @Test
    public void shouldFindCustomRoutesForPetStore() throws TemplateException, IOException {


        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfigPetStore", this.getClass().getClassLoader());
        endpointFinder.setApiLocation("src/test/open-api");

        Generator generator = new Generator();
        String generated = generator.generate("src/test/open-api/base.json", endpointFinder, Format.JSON);

        JSONMap jsonMap = JSONHelper.toJSONMap(generated);
        JSONMap paths = jsonMap.getMap("paths");

        the(paths.size()).shouldEqual(3);
        the(paths.getMap("/pet/findByStatus").size()).shouldEqual(1);
        the(paths.getMap("/pet/{petId}").size()).shouldEqual(3);
        the(paths.getMap("/pet").size()).shouldEqual(2);


        the(paths.getMap("/pet/findByStatus.get.responses").size()).shouldEqual(1);
        the(paths.getMap("/pet.post.responses").size()).shouldEqual(1);
        the(paths.getMap("/pet.put.responses").size()).shouldEqual(1);

        the(paths.getMap("/pet/{petId}.post.responses").size()).shouldEqual(1);
        the(paths.getMap("/pet/{petId}.get.responses").size()).shouldEqual(1);
        the(paths.getMap("/pet/{petId}.delete.responses").size()).shouldEqual(1);
    }
}
