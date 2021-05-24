package org.javalite.openapi;

import org.javalite.activeweb.ClassEndpointFinder;
import org.javalite.activeweb.HttpMethod;
import org.junit.Test;

import java.util.List;

import static org.javalite.activeweb.ClassEndpointFinder.getStandardEndpointDefinitions;
import static org.javalite.test.jspec.JSpec.$;
import static org.javalite.test.jspec.JSpec.the;

public class ClassEndpointFinderSpec {


    @Test
    public void shouldFindCustomEndpointsFromClasspath() {

        List<EndPointDefinition> endPointDefinitions = ClassEndpointFinder.getCustomEndpointDefinitions(this.getClass().getClassLoader());

        endPointDefinitions.forEach(System.out::println);
    }
    @Test
    public void shouldFindStandardEndpointsFromClasspath() {

        List<EndPointDefinition> endPointDefinitions = getStandardEndpointDefinitions(this.getClass().getClassLoader());

        the(endPointDefinitions.size()).shouldBeEqual(10);

        //true below
        $(contains(endPointDefinitions, HttpMethod.GET, "/test/index", "")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.POST, "/test/index", "")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.GET, "/test/foo", "")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.POST, "/test/save_person", "app.controllers.Person")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.GET, "/test/bar", "")).shouldBeTrue();

        $(contains(endPointDefinitions, HttpMethod.GET,     "/http_methods/index", "")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.POST,    "/http_methods/index", "")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.POST,    "/http_methods/do_post", "")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.PUT,     "/http_methods/do_put", "")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.OPTIONS, "/http_methods/do_options", "")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.PATCH,   "/http_methods/do_patch", "")).shouldBeTrue();
        $(contains(endPointDefinitions, HttpMethod.HEAD,    "/http_methods/do_head", "")).shouldBeTrue();

        //false below
        $(contains(endPointDefinitions, HttpMethod.GET, "/bad/get_age", "")).shouldBeFalse();
        $(contains(endPointDefinitions, HttpMethod.GET, "/bad/foo", "")).shouldBeFalse();
        $(contains(endPointDefinitions, HttpMethod.POST, "/bad/bar", "")).shouldBeFalse();

        //false below
        $(contains(endPointDefinitions, HttpMethod.GET, "/abstract/bar", "")).shouldBeFalse();
        $(contains(endPointDefinitions, HttpMethod.GET, "/bad/foo", "")).shouldBeFalse();
    }

    private boolean contains(List<EndPointDefinition> endPointDefinitions, HttpMethod method, String path, String argumentClassname) {
        for (EndPointDefinition d : endPointDefinitions) {
            if(d.hasMethod(method)
                    && d.getPath().equals(path)
                    && d.getArgumentClassName().equals(argumentClassname)){
                return true;
            }
        }
        return false;
    }
}
