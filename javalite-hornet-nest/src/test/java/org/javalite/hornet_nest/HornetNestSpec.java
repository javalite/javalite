package org.javalite.hornet_nest;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class HornetNestSpec {

    private static final String QUEUE_NAME = "queue1";
    private String filePath;

    @Before
    public void before() throws IOException {
        filePath = Files.createTempDirectory("hornet").toFile().getCanonicalPath();
    }

    @Test
    public void shouldProcessCommands() throws IOException {

        HornetNest hornetNest = new HornetNest(filePath, false, new QueueConfig(QUEUE_NAME, CommandListener.class, 50));

        //send 100 messages
        for(int i = 0; i < 100; i++){
            hornetNest.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i));
        }

        //messages will execute in about 2 seconds, because we send 100 messages, but only have 50 threads.
        //lets wait for 3 seconds, then validate result
        try {
            Thread.sleep(5000);
        } catch (Exception e) {}

        hornetNest.stop();
        a(HelloCommand.counter()).shouldBeEqual(100);
    }


    @Test
    public void shouldListTopCommands() throws Exception {
        HornetNest hornetNest = new HornetNest(filePath, false, new QueueConfig(QUEUE_NAME, CommandListener.class, 100));
        hornetNest.pause(QUEUE_NAME);

        //send 100 messages
        for(int i = 0; i < 100; i++){
            hornetNest.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i));
        }
        List<Command> commands = hornetNest.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(10);

        //drain queue
        hornetNest.resume(QUEUE_NAME);

        Thread.sleep(2000);

        commands = hornetNest.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(0);

        hornetNest.stop();
    }


    @Test
    public void shouldGetCommandsSynchronously() throws Exception {

        HornetNest hornetNest = new HornetNest(filePath, false, new QueueConfig(QUEUE_NAME, CommandListener.class, 0));

        for(int i = 0; i < 2; i++){
            hornetNest.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i));
        }

        a(hornetNest.receiveCommand(QUEUE_NAME).toString()).shouldBeEqual("Hello, Dolly 0");
        a(hornetNest.receiveCommand(QUEUE_NAME).toString()).shouldBeEqual("Hello, Dolly 1");
        
        Command c = hornetNest.receiveCommand(QUEUE_NAME, 100); // returns null because there are only 2 commands were sent
        a(c).shouldBeNull();
        
        hornetNest.stop();
    }


    @Test
    public void shouldRemoveMessages() throws Exception {

        HornetNest hornetNest = new HornetNest(filePath, false, new QueueConfig(QUEUE_NAME, CommandListener.class, 0));

        for(int i = 0; i < 2; i++){
            hornetNest.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i));
        }

        List<Command> commands = hornetNest.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(2);

        hornetNest.removeAllMessages(QUEUE_NAME);

        commands = hornetNest.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(0);

        hornetNest.stop();
    }
}
