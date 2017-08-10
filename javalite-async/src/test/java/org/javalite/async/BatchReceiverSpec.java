package org.javalite.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.javalite.test.jspec.JSpec.the;

/**
 * @author igor on 8/8/17.
 */
public class BatchReceiverSpec {
    private static final String QUEUE_NAME = "queue1";
    private String filePath;
    private Async async;

    @Before
    public void before() throws IOException {
        filePath = Files.createTempDirectory("async").toFile().getCanonicalPath();
        async = new Async(filePath, false, new QueueConfig(QUEUE_NAME));
        async.start();
        for (int i = 0; i < 50; i++) {
            async.sendTextMessage(QUEUE_NAME, "hello " + i);
        }
    }

    @After
    public void after(){
        async.stop();
    }

    @Test
    public void shouldReceiveMessages() throws JMSException {
        int count = 0;
        try (BatchReceiver br = async.getBatchReceiver(QUEUE_NAME, 100)) {
            List<String> messages = br.receiveTextMessages(500);
            for (String ignored : messages) {
                count++;

                // record to DB,
            }

            //commit to DB
            br.commit();
        }
        the(count).shouldBeEqual(50);
        async.stop();
    }
}
