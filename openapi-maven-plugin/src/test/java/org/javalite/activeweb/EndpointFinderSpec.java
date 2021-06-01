package org.javalite.activeweb;

import org.junit.Test;

import java.util.List;

import static org.javalite.activeweb.EndpointFinder.getStandardEndpointDefinitions;
import static org.javalite.common.Collections.list;
import static org.javalite.test.jspec.JSpec.$;
import static org.javalite.test.jspec.JSpec.the;

public class EndpointFinderSpec {

    @Test
    public void shouldFindCustomEndpointsFromClasspath() {

        List<EndPointDefinition> endPointDefinitions = EndpointFinder.getCustomEndpointDefinitions("app.config.RouteConfig1", this.getClass().getClassLoader());

        endPointDefinitions.forEach(System.out::println);

        the(endPointDefinitions.size()).shouldBeEqual(3);

        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET, "/hello",
                "app.controllers.TestController",
                "foo",
                "",
                ""))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.POST, "/person_save",
                "app.controllers.CustomController",
                "savePerson",
                "app.controllers.Person",
                "This is a simple stub for CustomController"))).shouldBeTrue();
        $(endPointDefinitions.contains(new EndPointDefinition(HttpMethod.GET,
                "/segments/{id}",
                "app.controllers.SegmentsController",
                "index",
                "",
                ""))).shouldBeTrue();
    }

    @Test
    public void shouldFindStandardEndpointsFromClasspath() {

        List<EndPointDefinition> endPointDefinitions = getStandardEndpointDefinitions(this.getClass().getClassLoader());

        the(endPointDefinitions.size()).shouldBeEqual(13);

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
    }
}
