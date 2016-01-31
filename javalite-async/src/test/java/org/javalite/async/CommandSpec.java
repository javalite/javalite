package org.javalite.async;

import org.junit.Test;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy on 1/31/16.
 */
public class CommandSpec {

    @Test
    public void shouldSerializeDeserialize(){
        HelloCommand helloCommand = new HelloCommand("Thanks for all the fish...");
        String xml = helloCommand.toXml();
        HelloCommand helloCommand1 = Command.fromXml(xml, HelloCommand.class);
        a(helloCommand1.getMessage()).shouldBeEqual("Thanks for all the fish...");
        a(helloCommand).shouldNotBeTheSameAs(helloCommand1);
    }
}
