package org.javalite.async;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.javalite.async.services.GreetingModule;
import org.javalite.json.JSONHelper;
import org.javalite.common.Util;
import org.javalite.common.Wait;
import org.javalite.test.SystemStreamUtil;
import org.javalite.test.jspec.ExceptionExpectation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.javalite.test.jspec.JSpec.*;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class AsyncSpec {

    private static final String QUEUE_NAME = "queue1";
    private String asyncRoot;
    private Async async;

    @Before
    public void before() throws IOException {
        asyncRoot = Files.createTempDirectory(UUID.randomUUID().toString()).toFile().getCanonicalPath();
        HelloCommand.reset();
        ContextCommand.reset();
    }

    @After
    public void after() throws IOException {
        async.stop();
        Util.recursiveDelete(Paths.get(asyncRoot));
    }

    @Test
    public void shouldProcessCommands()  {

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME, new CommandListener(), 50))
                .build();
        async.start();

        //send 100 messages
        for(int i = 0; i < 100; i++){
            async.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i), DeliveryMode.PERSISTENT);
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

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

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME, listener, 1))
                .build();
        async.start();

        long requiredTime = System.currentTimeMillis();

        for(int i = 0; i < 3; i++){
            requiredTime += 5000;
            async.send(QUEUE_NAME, new HelloCommand("Hello " + i), DeliveryMode.PERSISTENT, requiredTime);
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        a(deliveredTimes.size()).shouldBeEqual(3);

        for(Long time : deliveredTimes) {
            a(time >= requiredTime && time < requiredTime + 500).shouldBeTrue();
            requiredTime -= 5000;
        }
    }


    @Test
    public void shouldListTopCommands() {

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME, new CommandListener(), 100))
                .build();
        async.start();

        async.pause(QUEUE_NAME);

        //send 100 messages
        for(int i = 0; i < 100; i++){
            async.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i), DeliveryMode.PERSISTENT);
        }
        List<Command> commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(10);

        //drain queue
        async.resume(QUEUE_NAME);

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(0);

    }


    @Test
    public void shouldGetCommandsSynchronously() {

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME, new CommandListener(), 0))
                .build();

        async.start();
        for(int i = 0; i < 2; i++){
            async.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i), DeliveryMode.PERSISTENT);
        }

        a(async.receiveCommand(QUEUE_NAME, HelloCommand.class).getMessage()).shouldBeEqual("Hello, Dolly 0");
        a(async.receiveCommand(QUEUE_NAME, HelloCommand.class).getMessage()).shouldBeEqual("Hello, Dolly 1");

        Command c = async.receiveCommand(QUEUE_NAME, 100); // returns null because there were only 2 commands sent
        a(c).shouldBeNull();

    }


    @Test
    public void shouldRemoveMessages() {

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME, new CommandListener(), 0))
                .build();

        async.start();

        for(int i = 0; i < 2; i++){
            async.send(QUEUE_NAME, new HelloCommand("Hello, Dolly " + i), DeliveryMode.PERSISTENT);
        }

        List<Command> commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(2);

        a(commands.get(0).getJMSMessageID()).shouldNotBeNull();

        async.removeAllMessages(QUEUE_NAME);

        commands = async.getTopCommands(10, QUEUE_NAME);
        a(commands.size()).shouldBeEqual(0);

    }

    @Test
    public void shouldInjectDependencyIntoCommand(){

        Injector injector = Guice.createInjector(new GreetingModule());

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME, new CommandListener(), 1))
                .injector(injector)
                .build();

        async.start();

        async.send(QUEUE_NAME, new HelloInjectedCommand("The greeting is: "), DeliveryMode.PERSISTENT);

        Wait.waitFor(()-> HelloInjectedCommand.result != null);

        a(HelloInjectedCommand.result).shouldBeEqual("The greeting is: hi");
    }

    @Test
    public void shouldStartStopBroker() {

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME, new CommandListener(), 50))
                .build();

        async.start();
        for (int i = 0; i < 10; i++) {
            async.send(QUEUE_NAME, new HelloCommand("Message: " + i), DeliveryMode.PERSISTENT);
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        async.stop();
        a(HelloCommand.counter()).shouldBeEqual(10);

        //lets start second time
        async.start();

        for (int i = 0; i < 10; i++) {
            async.send(QUEUE_NAME, new HelloCommand("Message: " + i), DeliveryMode.PERSISTENT);
        }

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);

        a(HelloCommand.counter()).shouldBeEqual(20);
    }

    @Test
    public void shouldInjectDependencyIntoCommandListener() {

        Injector injector = Guice.createInjector(new GreetingModule());
        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME, new HelloCommandListener(), 1))
                .build();

        async.start();

        async.send(QUEUE_NAME, new HelloCommand("Hi, there"), DeliveryMode.PERSISTENT);

        //SEE ASSERTION INSIDE HelloCommandListener.

        Wait.waitFor(()-> async.getMessageCount(QUEUE_NAME) == 0);
    }

    @Test
    public void shouldMoveMessageToOtherQueue() throws JMSException {

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig("queue1"), new QueueConfig("queue2"))
                .build();

        async.start();

        async.sendTextMessage("queue1", "message test 1", DeliveryMode.PERSISTENT, 5, 0);
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
    }

    @Test
    public void shouldMoveMessagesToOtherQueue() {

        String queue1  = "queue1", queue2 = "queue2";

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig("queue1"), new QueueConfig("queue2"))
                .build();

        async.start();

        the(async.getMessageCount(queue1)).shouldBeEqual(0);
        the(async.getMessageCount(queue2)).shouldBeEqual(0);

        async.sendTextMessage(queue1, "message test 1", DeliveryMode.PERSISTENT);
        async.sendTextMessage(queue1, "message test 2", DeliveryMode.PERSISTENT);

        the(async.getMessageCount(queue1)).shouldBeEqual(2);

        int count = async.moveMessages(queue1, queue2);

        the(count).shouldBeEqual(2);

        the(async.getMessageCount(queue1)).shouldBeEqual(0);
        the(async.getMessageCount(queue2)).shouldBeEqual(2);

    }

    @Test
    public void shouldGetMessageCounts(){

        String queue1  = "queue1", queue2 = "queue2";
        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(queue1), new QueueConfig(queue2))
                .build();

        async.start();


        async.sendTextMessage(queue1, "message test 1", DeliveryMode.PERSISTENT);
        async.sendTextMessage(queue1, "message test 2", DeliveryMode.PERSISTENT);
        async.sendTextMessage(queue2, "message test 3", DeliveryMode.PERSISTENT);

        Map<String, Long> counts = async.getMessageCounts();

        the(counts.get(queue1)).shouldBeEqual(2);
        the(counts.get(queue2)).shouldBeEqual(1);
    }

    @Test
    public void shouldLogContext() {

        SystemStreamUtil.replaceOut();

        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME, new CommandListener(), 50))
                .build();

        async.start();
        async.send(QUEUE_NAME, new ContextCommand(true), DeliveryMode.PERSISTENT);
        async.send(QUEUE_NAME, new ContextCommand(false), DeliveryMode.PERSISTENT);

        Wait.waitFor(()-> ContextCommand.counter() == 2);

        String out = SystemStreamUtil.getSystemOut();

        String [] lines = Util.split(out, System.getProperty("line.separator"));

        String contextLine = getContextLine(lines);

        Map contextMap = JSONHelper.toMap(contextLine);

        Map context = (Map) contextMap.get("context");
        the(context.get("weight")).shouldBeEqual("35lb");

        SystemStreamUtil.restoreSystemOut();
    }

    private String getContextLine(String[] lines) {
        for (String line : lines) {
            if(line.contains("ActiveMQ-client-global-threads") && line.contains("weight")){
                return line;
            }
        }
        return null;
    }

    @Test
    public void shouldSendModelData()  {
        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME))
                .build();

        async.start();
        Book b = new Book();
        b.set("title", "Alice in Wonderland");
        async.send(QUEUE_NAME, new BookCommand(b), DeliveryMode.NON_PERSISTENT);
        BookCommand bc = async.receiveCommand(QUEUE_NAME, BookCommand.class);
        the(bc.getBook().get("title")).shouldBeEqual("Alice in Wonderland");
    }


    @Test
    public void should_trow_meaningful_message_if_command_has_no_default_constructor(){
        async = new Async.AsyncBuilder(asyncRoot)
                .queueConfigs(new QueueConfig(QUEUE_NAME))
                .build();
        async.start();
        async.send(QUEUE_NAME, new Hello2Command("Hello"), DeliveryMode.NON_PERSISTENT);

        try{
            async.receiveCommand(QUEUE_NAME, Hello2Command.class); //<<--- will cause an exception because Hello2Command does not have a
        }catch(Exception e){
            the(e).shouldBeA(AsyncException.class);
            the(e.getMessage()).shouldEqual("class org.javalite.async.Hello2Command does not have a required default constructor.");
        }
    }
}


