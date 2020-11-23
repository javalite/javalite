package org.javalite.async;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.javalite.async.services.GreetingModule;
import org.javalite.common.Util;
import org.javalite.common.Wait;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class AsyncBinarySpec {

    private static final String QUEUE_NAME = "queue1";
    private String asyncRoot;

    @Before
    public void before() throws IOException {
        asyncRoot = Files.createTempDirectory(UUID.randomUUID().toString()).toFile().getCanonicalPath();
        HelloCommand.reset();
    }

    @After
    public void after() throws IOException {
        Util.recursiveDelete(Paths.get(asyncRoot));
    }

    @Test
    public void shouldProcessCommands() throws IOException, InterruptedException {

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 50));

        async.setBinaryMode(true);
        async.start();

        //send 100 messages
        for(int i = 0; i < 100; i++){
            async.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i));
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);


        async.stop();
        a(HelloCommand.counter()).shouldBeEqual(100);
    }


    @Test
    public void shouldListTopCommands() throws Exception {
        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 100));

        async.setBinaryMode(true);
        async.start();

        async.pause(QUEUE_NAME);

        //send 100 messages
        for(int i = 0; i < 100; i++){
            async.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i));
        }
        List<Command> commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(10);

        //drain queue
        async.resume(QUEUE_NAME);

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);


        commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(0);

        async.stop();
    }


    @Test
    public void shouldGetCommandsSynchronously() throws Exception {

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 0));

        async.setBinaryMode(true);
        async.start();
        for(int i = 0; i < 2; i++){
            async.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i));
        }

        a(async.receiveCommand(QUEUE_NAME, HelloCommand.class).getMessage()).shouldBeEqual("Hello, Dolly 0");
        a(async.receiveCommand(QUEUE_NAME, HelloCommand.class).getMessage()).shouldBeEqual("Hello, Dolly 1");

        Command c = async.receiveCommand(QUEUE_NAME, 100); // returns null because there were only 2 commands sent
        a(c).shouldBeNull();
        
        async.stop();
    }


    @Test
    public void shouldRemoveMessages() throws Exception {

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 0));

        async.setBinaryMode(true);
        async.start();

        for(int i = 0; i < 2; i++){
            async.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i));
        }

        List<Command> commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(2);

        int count = async.removeAllMessages(QUEUE_NAME);

        the(count).shouldBeEqual(2);

        commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(0);

        async.stop();
    }

    @Test
    public void shouldInjectDependencyIntoCommand() throws InterruptedException {

        Injector injector = Guice.createInjector(new GreetingModule());
        Async async = new Async(asyncRoot, false, injector, new QueueConfig(QUEUE_NAME, new CommandListener(), 1));

        async.setBinaryMode(true);
        async.start();

        async.send(QUEUE_NAME, new HelloInjectedCommand("The greeting is: "));

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        async.stop();
        a(HelloInjectedCommand.result).shouldBeEqual("The greeting is: hi");
    }

    @Test
    public void shouldStartStopBroker() throws IOException, InterruptedException {

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 50));
        async.setBinaryMode(true);
        async.start();
        for(int i = 0; i < 10; i++){
            async.send(QUEUE_NAME, new HelloCommand("Message: " + i));
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        async.stop();
        a(HelloCommand.counter()).shouldBeEqual(10);

        //lets start second time
        async.start();

        for(int i = 0; i < 10; i++){
            async.send(QUEUE_NAME, new HelloCommand("Message: " + i));
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);
        async.stop();
        a(HelloCommand.counter()).shouldBeEqual(20);
    }

    @Test
    public void shouldInjectDependencyIntoCommandListener() throws InterruptedException {

        Injector injector = Guice.createInjector(new GreetingModule());
        Async async = new Async(asyncRoot, false, injector, new QueueConfig(QUEUE_NAME, new HelloCommandListener(), 1));
        async.setBinaryMode(true);
        async.start();

        async.send(QUEUE_NAME, new HelloCommand("Hi, there"));

        //SEE ASSERTION INSIDE HelloCommandListener.

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);
        async.stop();
    }
}
