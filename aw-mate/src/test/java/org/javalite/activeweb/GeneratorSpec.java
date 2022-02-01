package org.javalite.activeweb;

import freemarker.template.TemplateException;
import org.javalite.activeweb.mojo.Generator;
import org.javalite.json.JSONHelper;
import org.javalite.json.JSONList;
import org.javalite.json.JSONMap;
import org.junit.Test;

import java.io.IOException;

import static org.javalite.test.jspec.JSpec.the;


public class GeneratorSpec {

    @Test
    public void shouldGenerateJSON() throws TemplateException, IOException {

        Generator generator = new Generator();

        EndpointFinder endpointFinder = new EndpointFinder(getClass().getClassLoader());
        endpointFinder.setApiLocation("src/test/open-api");
        String generated = generator.generate("src/test/open-api/base.json", endpointFinder, Format.JSON);


        JSONMap root = JSONHelper.toJSONMap(generated);

        JSONMap paths = root.getMap("paths");

        //Lets do some spot checks:
        the(paths.size()).shouldBeEqual(19);
        the(paths.get("/custom/index.get.summary")).shouldEqual("Some custom stuff 11");
        the(paths.get("/custom/index.get.responses.200.description")).shouldEqual("<table> <tr> <td>First name</td> <td>Last name</td> </tr> <tr> <td style='background-color: aqua'>Freddie</td> <td>Mercury</td> </tr> </table>");

        the(paths.get("/custom/index.post.summary")).shouldEqual("Show API version details");
        the(paths.get("/custom/index.get.summary")).shouldEqual("Some custom stuff 11");
        the(paths.getMap("/custom/index").size()).shouldEqual(2);

        //Deep test:
        the(paths.getList("/segments/foobar_2.get.responses.200.content.application/json.examples.foo.value.version.media-types").getMap(0).get("type")).shouldEqual("application/vnd.openstack.compute+xml;version=2");

    }
}
