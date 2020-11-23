package org.javalite.async;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.javalite.async.services.GreetingModule;
import org.javalite.common.JsonHelper;
import org.javalite.common.Util;
import org.javalite.common.Wait;
import org.javalite.test.SystemStreamUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class AsyncSpec {

    private static final String QUEUE_NAME = "queue1";
    private String asyncRoot;

    @Before
    public void before() throws IOException {
        asyncRoot = Files.createTempDirectory(UUID.randomUUID().toString()).toFile().getCanonicalPath();
        HelloCommand.reset();
        SystemStreamUtil.replaceOut();
    }

    @After
    public void after() throws IOException {
        Util.recursiveDelete(Paths.get(asyncRoot));
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldProcessCommands()  {

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 50));

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
    public void shouldProcessCommandAtRequiredTime() {

        final List<Long> deliveredTimes = Collections.synchronizedList(new ArrayList<>());

        CommandListener listener = new CommandListener() {
            @Override
            public <T extends Command> void onCommand(T command) {
                super.onCommand(command);
                deliveredTimes.add(0, System.currentTimeMillis());
            }
        };

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, listener, 1));

        async.start();

        long requiredTime = System.currentTimeMillis();

        for(int i = 0; i < 3; i++){
            requiredTime += 5000;
            async.send(QUEUE_NAME, new HelloCommand("Hello " + i), new Date(requiredTime));
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        async.stop();

        a(deliveredTimes.size()).shouldBeEqual(3);

        for(Long time : deliveredTimes) {
            a(time >= requiredTime && time < requiredTime + 500).shouldBeTrue();
            requiredTime -= 5000;
        }

    }


    @Test
    public void shouldListTopCommands() {
        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 100));

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
    public void shouldGetCommandsSynchronously() {

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 0));

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
    public void shouldRemoveMessages() {

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 0));

        async.start();

        for(int i = 0; i < 2; i++){
            async.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i));
        }

        List<Command> commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(2);

        a(commands.get(0).getJMSMessageID()).shouldNotBeNull();

        async.removeAllMessages(QUEUE_NAME);

        commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(0);

        async.stop();
    }

    @Test
    public void shouldInjectDependencyIntoCommand(){

        Injector injector = Guice.createInjector(new GreetingModule());
        Async async = new Async(asyncRoot, false, injector, new QueueConfig(QUEUE_NAME, new CommandListener(), 1));

        async.start();

        async.send(QUEUE_NAME, new HelloInjectedCommand("The greeting is: "));

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        async.stop();
        a(HelloInjectedCommand.result).shouldBeEqual("The greeting is: hi");
    }

    @Test
    public void shouldStartStopBroker() {

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 50));
        async.start();
        for (int i = 0; i < 10; i++) {
            async.send(QUEUE_NAME, new HelloCommand("Message: " + i));
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        async.stop();
        a(HelloCommand.counter()).shouldBeEqual(10);

        //lets start second time
        async.start();

        for (int i = 0; i < 10; i++) {
            async.send(QUEUE_NAME, new HelloCommand("Message: " + i));
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        async.stop();
        a(HelloCommand.counter()).shouldBeEqual(20);
    }

    @Test
    public void shouldInjectDependencyIntoCommandListener() {

        Injector injector = Guice.createInjector(new GreetingModule());
        Async async = new Async(asyncRoot, false, injector, new QueueConfig(QUEUE_NAME, new HelloCommandListener(), 1));
        async.start();

        async.send(QUEUE_NAME, new HelloCommand("Hi, there"));

        //SEE ASSERTION INSIDE HelloCommandListener.

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);
        async.stop();
    }

    @Test
    public void shouldMoveMessageToOtherQueue() throws JMSException {

        Async async = new Async(asyncRoot, false, new QueueConfig("queue1"), new QueueConfig("queue2"));
        async.start();

        async.sendTextMessage("queue1", "message test 1");
        Message m1 = async.lookupMessage("queue1");
        the(m1).shouldNotBeNull();

        Message m2 = async.lookupMessage("queue2");
        the(m2).shouldBeNull();

        boolean result = async.moveMessage(m1.getJMSMessageID(), "queue1", "queue2");

        the(result).shouldBeTrue();

        m1 = async.lookupMessage("queue1");
        the(m1).shouldBeNull();

        m2 = async.lookupMessage("queue2");
        the(m2).shouldNotBeNull();

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);
        async.stop();
    }

    @Test
    public void shouldMoveMessagesToOtherQueue() {

        String queue1  = "queue1", queue2 = "queue2";
        Async async = new Async(asyncRoot, false, new QueueConfig(queue1), new QueueConfig(queue2));
        async.start();

        the(async.getMessageCount(queue1)).shouldBeEqual(0);
        the(async.getMessageCount(queue2)).shouldBeEqual(0);

        async.sendTextMessage(queue1, "message test 1");
        async.sendTextMessage(queue1, "message test 2");

        the(async.getMessageCount(queue1)).shouldBeEqual(2);

        int count = async.moveMessages(queue1, queue2);

        the(count).shouldBeEqual(2);

        the(async.getMessageCount(queue1)).shouldBeEqual(0);
        the(async.getMessageCount(queue2)).shouldBeEqual(2);

        async.stop();
    }

    @Test
    public void shouldGetMessageCounts(){

        String queue1  = "queue1", queue2 = "queue2";
        Async async = new Async(asyncRoot, false, new QueueConfig(queue1), new QueueConfig(queue2));
        async.start();


        async.sendTextMessage(queue1, "message test 1");
        async.sendTextMessage(queue1, "message test 2");

        async.sendTextMessage(queue2, "message test 3");

        Map<String, Long> counts = async.getMessageCounts();

        the(counts.get(queue1)).shouldBeEqual(2);
        the(counts.get(queue2)).shouldBeEqual(1);

        async.stop();
    }

    @Test
    public void shouldLogContext() {

        Async async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME, new CommandListener(), 50));
        async.start();
        async.send(QUEUE_NAME, new ContextCommand(true));
        async.send(QUEUE_NAME, new ContextCommand(false));

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        async.stop();

        String out = SystemStreamUtil.getSystemOut();

        String [] lines = Util.split(out, System.getProperty("line.separator"));

        String contextLine = getContextLine(lines);
        String nonContextLine = getNonContextLine(lines);

        Map contextMap = JsonHelper.toMap(contextLine);
        Map nonContextMap = JsonHelper.toMap(nonContextLine);

        Map context = (Map) contextMap.get("context");
        the(context.get("weight")).shouldBeEqual("35lb");

        the(nonContextMap.containsKey("context")).shouldBeFalse();
    }

    private String getContextLine(String[] lines) {
        for (String line : lines) {
            if(line.contains("ActiveMQ-client-global-threads") && line.contains("weight")){
                return line;
            }
        }
        return null;
    }

    private String getNonContextLine(String[] lines) {
        for (String line : lines) {
            if(line.contains("ActiveMQ-client-global-threads") && !line.contains("weight")){
                return line;
            }
        }
        return null;
    }
}


