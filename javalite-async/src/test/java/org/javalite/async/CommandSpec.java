package org.javalite.async;

import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 1/31/16.
 */
public class CommandSpec {

    @Test
    public void shouldSerializeDeserializeXML(){
        HelloCommand helloCommand = new HelloCommand("Thanks for all the fish...");
        String xml = helloCommand.toXml();
        HelloCommand helloCommand1 = Command.fromXml(xml);
        a(helloCommand1.getMessage()).shouldBeEqual("Thanks for all the fish...");
        a(helloCommand).shouldNotBeTheSameAs(helloCommand1);
    }

    @Test
    public void shouldSerializeDeserializeBinary() throws IOException {
        HelloCommand helloCommand = new HelloCommand("Thanks for all the fish...");
        byte[] bytes  = helloCommand.toBytes();

        HelloCommand helloCommand1 = Command.fromBytes(bytes);
        a(helloCommand1.getMessage()).shouldBeEqual("Thanks for all the fish...");
        a(helloCommand).shouldNotBeTheSameAs(helloCommand1);
    }

    @Test
    public void shouldGenerateParams() throws IOException {
        HelloCommand helloCommand = new HelloCommand("Thanks for all the fish...");
        Map<String, String> params =  helloCommand.getParams();
        the(params.get("class")).shouldBeEqual("HelloCommand");
        the(params.get("message")).shouldBeEqual("Thanks for all the fish...");
    }
}
