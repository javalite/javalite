package org.javalite.activeweb;

import freemarker.template.TemplateException;
import org.javalite.activeweb.mojo.Generator;
import org.javalite.json.JSONHelper;
import org.javalite.json.JSONMap;
import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.javalite.test.jspec.JSpec.the;

public class EndpointFinderSpec {


    private static final String BASE_TEMPLATE = """
                {
                      "openapi": "3.0.0",
                      "info": {
                        "title": "Simple API overview",
                        "version": "3.0.0"
                      },
                      "paths":{
                      
                      }
                  }""";


    @Test
    public void should_find_api_with_strictMode() {

        SystemStreamUtil.replaceError();
        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig1", this.getClass().getClassLoader());
        endpointFinder.setApiLocation("src/test/open-api");

        String formattedJSONString = endpointFinder.getOpenAPIDocs(BASE_TEMPLATE, Format.JSON);
        JSONMap apiMap= JSONHelper.toMap(formattedJSONString);
        JSONMap paths = apiMap.getMap("paths");
        the(paths.size()).shouldBeEqual(3);
        the(paths.get("/custom.post.summary")).shouldBeEqual("Show API version details - CustomController#index - @POST annotation");
        the(paths.get("/person_save.post.summary")).shouldBeEqual("Show API version details - CustomController#savePerson - @POST annotation");
        the(paths.get("/hello.post.summary")).shouldBeEqual("Inherited method!!!");

        String x = SystemStreamUtil.getSystemErr();

        the(x).shouldContain("Failed to find a method for controller: 'class app.controllers.HomeController' and action: 'team'. Check your RouteConfig class.");

        SystemStreamUtil.restoreSystemErr();
    }

    @Test
    public void should_find_api_in_annotations_and_files_with_standard_and_custom_routes() {

        EndpointFinder endpointFinder = new EndpointFinder(this.getClass().getClassLoader());
        endpointFinder.setApiLocation("src/test/open-api2");

        String formattedJSONString = endpointFinder.getOpenAPIDocs(BASE_TEMPLATE, Format.JSON);
        JSONMap apiMap= JSONHelper.toMap(formattedJSONString);
        JSONMap paths = apiMap.getMap("paths");
        the(paths.keySet().size()).shouldBeEqual(19);

        the(paths.get("/custom/index.post.summary")).shouldBeEqual("Show API version details - CustomController#index - @POST annotation");
        the(paths.get("/custom/save_person.post.summary")).shouldBeEqual("Show API version details - CustomController#savePerson - @POST annotation");

        the(paths.get("/http_methods/do_head.head.description")).shouldBeEqual("docs for doHead");
        the(paths.get("/http_methods/do_put.put.description")).shouldBeEqual("docs for doPut");

        the(paths.get("/pet/update.put.responses.200.description")).shouldBeEqual("200 response");
        the(paths.get("/pet/find_by_status.get.responses.200.description")).shouldBeEqual("200 response GET");
        the(paths.get("/pet/add.post.responses.200.description")).shouldBeEqual("200 response POST");
        the(paths.get("/pet/update_pet.post.responses.200.description")).shouldBeEqual("200 response POST");
        the(paths.get("/pet/delete_pet.delete.responses.200.description")).shouldBeEqual("200 response DELETE");
        the(paths.get("/pet/get_pet.get.responses.200.description")).shouldBeEqual("200 response GET");

        the(paths.get("/segments/foobar_2.get.responses.200.description")).shouldBeEqual("200 all good"); //<<-- from a file!
        the(paths).shouldContain("/segments/foobar");
        the(paths).shouldContain("/test/foo");

        //from RouteConfig - custom routes:
        the(paths.get("/hello.post.summary")).shouldBeEqual("Inherited method!!!");
        the(paths.get("/person_save.post.summary")).shouldBeEqual("Show API version details - CustomController#savePerson - @POST annotation");

        //some random deeper spot checks:
        the(paths.get("/http_methods/do_put.put.description")).shouldEqual("docs for doPut");
        the(paths.get("/test/foo.post.summary")).shouldEqual("Inherited method!!!");
    }

    @Test
    public void should_not_find_standard_endpoints_with_strict_mode() {

        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig1", this.getClass().getClassLoader());
        List<EndPointDefinition> endPointDefinitions = endpointFinder.getStandardEndpointDefinitions(Format.JSON);
        the(endPointDefinitions.size()).shouldBeEqual(0);
    }

    //API Location is not set! The routes and endpoints are reflected from  code.
    @Test
    public void should_generate_with_standard_and_custom_routes() {

        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig", this.getClass().getClassLoader());


        String formattedJSONString = endpointFinder.getOpenAPIDocs(BASE_TEMPLATE, Format.JSON);

        JSONMap apiMap= JSONHelper.toMap(formattedJSONString);
        JSONMap paths = apiMap.getMap("paths");
        the(paths.keySet().size()).shouldBeEqual(18);

        //NOTE: we have 14 items here, 12 coming from controllers, and another 2 from the RouteConfig.
        the(paths).shouldContain("/custom/index");
        the(paths.get("/custom/index.post.summary")).shouldBeEqual("Show API version details - CustomController#index - @POST annotation"); // <<---- this is coming from annotation!!

        the(paths).shouldContain("/http_methods/do_head");
        the(paths).shouldContain("/http_methods/do_put");
        the(paths).shouldContain("/pet/update");
        the(paths).shouldContain("/pet/find_by_status");
        the(paths).shouldContain("/custom/save_person");
        the(paths).shouldContain("/pet/add");
        the(paths).shouldContain("/pet/update_pet");
        the(paths).shouldContain("/person_save");
        the(paths).shouldContain("/pet/delete_pet");
        the(paths).shouldContain("/custom/index");
        the(paths).shouldContain("/pet/get_pet");
        the(paths).shouldContain("/hello");
        the(paths).shouldContain("/segments/foobar_2");
        the(paths).shouldContain("/test/foo");

        //some deeper spot checks:
        the(paths.get("/http_methods/do_put.put.description")).shouldEqual("docs for doPut");
        the(paths.get("/test/foo.post.summary")).shouldEqual("Inherited method!!!");
        the(paths.get("/pet/update.put.responses.200.description")).shouldEqual("200 response");
    }

    @Test
    public void should_find_api_from_files_with_strictMode() {
        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig2", this.getClass().getClassLoader());

        endpointFinder.setApiLocation("src/test/open-api");
        List<EndPointDefinition> endPointDefinitions = endpointFinder.getCustomEndpointDefinitions(Format.JSON);

        the(endPointDefinitions.size()).shouldBeEqual(1);

        the(endPointDefinitions.get(0).getHTTPMethods().size()).shouldBeEqual(1);
        the(endPointDefinitions.get(0).hasOpenAPI()).shouldBeTrue();

        the(endPointDefinitions.get(0).getEndpointMethods().size()).shouldEqual(1);

        the(endPointDefinitions.get(0).getEndpointMethods().get(0).getHttpMethod()).shouldEqual(HttpMethod.GET);
        the(endPointDefinitions.get(0).getEndpointMethods().get(0).getAPIAsMap().get("responses.200.description")).shouldEqual("200 all good");
    }


    @Test
    public void should_fail_due_to_duplicate_docs(){

         try{
             EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig3", this.getClass().getClassLoader());
             endpointFinder.setApiLocation("src/test/open-api");
             endpointFinder.getCustomEndpointDefinitions(Format.JSON);
         }catch(OpenAPIException exception){


             the(exception.getMessage()).shouldBeEqual("The action: public void app.controllers.SegmentsController.foobar2() " +
                     "contains the OpenAPI documentation in a corresponding file, as well as in the annotation GET. Only one place of record is allowed.");
             return;
         }

         throw new RuntimeException("This test failed");
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
        String apiLocation = "src/test/open-api/";
        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfigPetStore", this.getClass().getClassLoader());
        endpointFinder.setApiLocation(apiLocation);

        Generator generator = new Generator();
        String generated = generator.generate(apiLocation, "base.json", endpointFinder, Format.JSON);

        JSONMap jsonMap = JSONHelper.toMap(generated);
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

    @Test
    public void should_find_routes_for_restful_controllers() throws TemplateException, IOException {

        String apiLocation = "src/test/open-api3";

        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig5", this.getClass().getClassLoader());
        endpointFinder.setApiLocation(apiLocation);

        Generator generator = new Generator();
        String generated = generator.generate(apiLocation, "base.json", endpointFinder, Format.JSON);

        JSONMap jsonMap = JSONHelper.toMap(generated);
        JSONMap paths = jsonMap.getMap("paths");

        the(paths.get("/pet_rest.post.responses.200.description")).shouldEqual("Creates a pet");
        the(paths.get("/pet_rest.get.responses.200.description")).shouldEqual("List all pets");
        the(paths.get("/pet_rest/{id}/edit_form.get.responses.200.description")).shouldEqual("Displays a form for editing an existing pet");
        the(paths.get("/pet_rest/{id}.get.responses.200.description")).shouldEqual("Get a pet by ID");
        the(paths.get("/pet_rest/{id}.delete.responses.200.description")).shouldEqual("Delete a pet by ID");
        the(paths.get("/pet_rest/{id}.put.responses.200.description")).shouldEqual("Update a specific pet by ID");
        the(paths.get("/pet_rest/new_form.get.responses.200.description")).shouldEqual("Displays a form for creation of a new pet");
    }
}
