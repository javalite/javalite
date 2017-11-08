package org.javalite.async.pooltest;

import org.javalite.async.Async;
import org.javalite.async.CommandListener;
import org.javalite.async.QueueConfig;
import org.javalite.common.Util;

import java.io.IOException;
import java.nio.file.Files;

/**
 *
 *
 * To execute:
 *  <pre>
 *      mvn exec:java -Dexec.mainClass="org.javalite.async.pooltest.TestSend"
 * </pre>
 * @author igor on 11/1/17.
 */
public class TestSend {

    public static final int MESSAGES_PER_THREAD = 1000;
    public static final int SENDING_THREAD_COUNT = 50;
    public static final int LISTENER_THREAD_COUNT = 50;

    public static void main(String[] args) throws IOException {

        testSendMethod();
    }

    private static void testSendMethod() throws IOException {
        String QUEUE_NAME = "queue1";
        String filePath = Files.createTempDirectory("async").toFile().getCanonicalPath();
        Async async = new Async(filePath, false, new QueueConfig(QUEUE_NAME, new CommandListener(), LISTENER_THREAD_COUNT));
        String loremIpsum = Util.readResource("/lorem-ipsum.txt");
        async.start();
        HelloPerformanceCommand.START = System.currentTimeMillis();
        Runnable r = () -> {
            for(int i = 0; i < MESSAGES_PER_THREAD; i++){
                async.send(QUEUE_NAME, new HelloPerformanceCommand(loremIpsum + i));
                System.out.println("sent....");
            }
        };

        for(int i = 0; i < SENDING_THREAD_COUNT; i++){
            Thread t = new Thread(r);
            t.start();

            try {

                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
