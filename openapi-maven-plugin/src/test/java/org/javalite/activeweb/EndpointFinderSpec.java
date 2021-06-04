package org.javalite.activeweb;

import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

import java.util.List;

import static org.javalite.activeweb.TablePrinter.printEndpointDefinitions;
import static org.javalite.common.Collections.list;
import static org.javalite.test.jspec.JSpec.$;
import static org.javalite.test.jspec.JSpec.the;

public class EndpointFinderSpec {

    @Test
    public void shouldFindCustomEndpointsFromClasspath() {

        SystemStreamUtil.replaceError();
        SystemStreamUtil.replaceOut();

        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig1", this.getClass().getClassLoader());
        List<EndPointDefinition> endPointDefinitions = endpointFinder.getCustomEndpointDefinitions();
        printEndpointDefinitions("Title", endPointDefinitions);

        the(endPointDefinitions.size()).shouldBeEqual(5);

        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/hello", "app.controllers.TestController", "foo", "", ""))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.POST, "/person_save", "app.controllers.CustomController", "savePerson", "app.controllers.Person", "This is a simple stub for CustomController"))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/segments/{id}", "app.controllers.SegmentsController", "index", "", ""))).shouldBeTrue();

        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/about", "app.controllers.HomeController", "about", "", ""))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/solutions", "app.controllers.HomeController", "solutions", "", ""))).shouldBeTrue();

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
        List<EndPointDefinition> endPointDefinitions = endpointFinder.getStandardEndpointDefinitions();

        printEndpointDefinitions("Title", endPointDefinitions);
        the(endPointDefinitions.size()).shouldBeEqual(15);

        //true below
        $(endPointDefinitions.contains(new EndPointDefinition(list(HttpMethod.GET, HttpMethod.POST), "/test/index", "app.controllers.TestController", "index", "", ""))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/test/foo", "app.controllers.TestController", "foo", "", "" ))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.POST, "/test/save_person", "app.controllers.TestController", "savePerson", "app.controllers.Person", "" ))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/test/bar", "app.controllers.TestController", "bar", "", "" ))).shouldBeTrue();

        $(endPointDefinitions.contains(new EndPointDefinition(list(HttpMethod.GET,HttpMethod.POST), "/http_methods/index", "app.controllers.HttpMethodsController", "index", "", ""))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.POST, "/http_methods/do_post", "app.controllers.HttpMethodsController", "doPost", "", ""))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.PUT, "/http_methods/do_put", "app.controllers.HttpMethodsController", "doPut", "", "docs for doPut"))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.OPTIONS, "/http_methods/do_options", "app.controllers.HttpMethodsController", "doOptions", "", ""))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.PATCH, "/http_methods/do_patch", "app.controllers.HttpMethodsController", "doPatch", "", ""))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.HEAD, "/http_methods/do_head", "app.controllers.HttpMethodsController", "doHead", "", "docs for doHead"))).shouldBeTrue();

        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/segments/index", "app.controllers.SegmentsController", "index", "", ""))).shouldBeTrue();

        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/custom/index", "app.controllers.CustomController", "index", "", ""))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.POST, "/custom/save_person", "app.controllers.CustomController", "savePerson", "app.controllers.Person", "This is a simple stub for CustomController"))).shouldBeTrue();

        //false below
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/bad/get_age", "app.controllers.CustomController", "", "", ""))).shouldBeFalse();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/bad/foo", "app.controllers.CustomController", "", "", ""))).shouldBeFalse();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.POST, "/bad/bar", "app.controllers.CustomController", "", "", ""))).shouldBeFalse();

        //false below
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/abstract/bar", "app.controllers.AbstractController", "", "", ""))).shouldBeFalse();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/bad/foo", "app.controllers.CustomController", "", "", ""))).shouldBeFalse();

        the(SystemStreamUtil.getSystemOut()).shouldContain("""
                +--------+--------------------------+--------------+---------------------------------------+------------------------------------+
                | Number | Path                     | HTTP Methods | Controller                            | Method                             |
                +--------+--------------------------+--------------+---------------------------------------+------------------------------------+
                | 1      | /custom/index            | GET          | app.controllers.CustomController      | index()                            |
                | 2      | /custom/save_person      | POST         | app.controllers.CustomController      | savePerson(app.controllers.Person) |
                | 3      | /home/about              | GET          | app.controllers.HomeController        | about()                            |
                | 4      | /home/solutions          | GET          | app.controllers.HomeController        | solutions()                        |
                | 5      | /http_methods/index      | GET,POST     | app.controllers.HttpMethodsController | index()                            |
                | 6      | /http_methods/do_post    | POST         | app.controllers.HttpMethodsController | doPost()                           |
                | 7      | /http_methods/do_put     | PUT          | app.controllers.HttpMethodsController | doPut()                            |
                | 8      | /http_methods/do_head    | HEAD         | app.controllers.HttpMethodsController | doHead()                           |
                | 9      | /http_methods/do_options | OPTIONS      | app.controllers.HttpMethodsController | doOptions()                        |
                | 10     | /http_methods/do_patch   | PATCH        | app.controllers.HttpMethodsController | doPatch()                          |
                | 11     | /segments/index          | GET          | app.controllers.SegmentsController    | index()                            |
                | 12     | /test/index              | GET,POST     | app.controllers.TestController        | index()                            |
                | 13     | /test/foo                | GET          | app.controllers.TestController        | foo()                              |
                | 14     | /test/save_person        | POST         | app.controllers.TestController        | savePerson(app.controllers.Person) |
                | 15     | /test/bar                | GET          | app.controllers.TestController        | bar()                              |
                +--------+--------------------------+--------------+---------------------------------------+------------------------------------+""");

        //we still get custom defined  in a default RouteConfig
        List<EndPointDefinition> customDefinitions =  endpointFinder.getCustomEndpointDefinitions();
        the(customDefinitions.size()).shouldBeEqual(2);
        $(customDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/hello", "app.controllers.TestController", "foo", "", ""))).shouldBeTrue();
        $(customDefinitions.contains(new EndPointDefinition(HttpMethod.POST, "/person_save", "app.controllers.CustomController", "savePerson", "app.controllers.Person", "This is a simple stub for CustomController"))).shouldBeTrue();

        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void should_not_find_standard_endpoints_with_strictMode() {

        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig1", this.getClass().getClassLoader());
        List<EndPointDefinition> endPointDefinitions = endpointFinder.getStandardEndpointDefinitions();
        the(endPointDefinitions.size()).shouldBeEqual(0);
    }

    @Test
    public void shouldCollectOpenAPIDocs(){

        EndpointFinder endpointFinder = new EndpointFinder("app.config.RouteConfig", this.getClass().getClassLoader());
        List<EndPointDefinition> customDefinitions = endpointFinder.getCustomEndpointDefinitions();
        List<EndPointDefinition> standardDefinitions = endpointFinder.getStandardEndpointDefinitions();

        String baseTemplate = """
                header
                {{content}}
                footer""";

        //NOTE: the doc: "Description  of the API end point to save a Person object" is mentioned twice because it is found both as a custom as well a standard endpoint.
        // This is an expected behavior!
        the(endpointFinder.getOpenAPIDocs(customDefinitions, standardDefinitions, baseTemplate, ", ")).shouldBeEqual("""
                header
                Generic description for an index endpoint, Description  of the API end point to save a Person object, docs for doPut, docs for doHead, Description  of the API end point to save a Person object
                footer""");
    }
}
