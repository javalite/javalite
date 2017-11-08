package org.javalite.async.pooltest;

import org.javalite.async.Async;
import org.javalite.async.Command;
import org.javalite.async.QueueConfig;
import org.javalite.common.Util;

import java.io.IOException;
import java.nio.file.Files;

/**
 *
 *
 * To execute:
 *  <pre>
 *      mvn exec:java -Dexec.mainClass="org.javalite.async.pooltest.TestReceive"
 *  </pre>
 * @author igor on 11/1/17.
 */
public class TestReceive {

    public static final int MESSAGES_PER_THREAD = 1000;
    public static final int SENDING_THREAD_COUNT = 50;


    public static void main(String[] args) throws IOException {
        testReceiveMethod();
    }


    private static void testReceiveMethod() throws IOException {
        String QUEUE_NAME = "queue1";
        String filePath = Files.createTempDirectory("async").toFile().getCanonicalPath();
        Async async = new Async(filePath, true, new QueueConfig(QUEUE_NAME));
        String loremIpsum = Util.readResource("/lorem-ipsum.txt");
        async.start();

        //SEND MESSAGES IN MANY THREADS
        Runnable sender = () -> {
            for(int i = 0; i < MESSAGES_PER_THREAD; i++){
                async.send(QUEUE_NAME, new HelloPerformanceCommand(loremIpsum + i));
                System.out.println("sent....");
            }
        };

        for(int i = 0; i < SENDING_THREAD_COUNT; i++){
            Thread t = new Thread(sender);
            t.start();
        }

        long start  = System.currentTimeMillis();

        for (int i = 0; true; i++) {
            Command command = async.receiveCommand(QUEUE_NAME, 1000);
            if(command != null){
                System.out.println("received: " + command);
            }else {
                System.out.println("completed, took " + (System.currentTimeMillis() - start) + " milliseconds");

                //lets wait toll sessions are cleaned by the watcher:

                System.out.println("waiting to close sessions...");
                try {
                    Thread.sleep(1000 * 4 * 60); // wait 2 minutes
                } catch (InterruptedException ignore) {}

                async.stop();
                System.exit(0);
            }
        }


    }
}
