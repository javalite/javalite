package org.javalite.hornet_nest;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class HornetNestSpec {

    @Test
    public void shouldProcessCommands() throws IOException {

        Path path = Files.createTempDirectory("hornet");

        HornetNest hornetNest =
                new HornetNest(path.toFile().getCanonicalPath(), false,
                        new QueueConfig("queuea", CommandListener.class, 50));

        //send 100 messages
        for(int i = 0; i < 100; i++){
            hornetNest.send("queuea", new HelloCommand("Hello, Dolly " + i));
        }

        //messages will execute in about 2 seconds, because we send 100 messages, but only have 50 threads.
        //lets wait for 3 seconds, then validate result
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }

        hornetNest.stop();
        a(HelloCommand.counter()).shouldBeEqual(100);
    }
}
