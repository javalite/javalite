package org.javalite.async;

import org.javalite.common.Util;

import javax.jms.*;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Use to batch-read text messages from Async in a cont5ext of a transaction.
 * Usually used to pass JSON or XML documents.
 *
 * <br>Typical usage pattern:
 *
 * <pre>
 //send messages:
 async.sendTextMessage(QUEUE_NAME, jsonDocument);
 ... // repeat

 //receive messages:
 BatchReceiver br = async.getBatchReceiver(QUEUE_NAME, 100);
 List<String> messages = br.receiveTextMessages(500);
 //... process messages, commit to database
 br.commit(); // <<< -- deletes messages from queue
 br.close();
 * </pre>
 *
 * <em>*** NEVER FORGET TO COMMIT AND CLOSE! ***</em>
 *
 * @author igor on 8/8/17.
 */
public class BatchReceiver implements Closeable {

    private long timeout;
    private Session session;
    private MessageConsumer consumer;


    protected BatchReceiver(String queueName, long timeout, Connection connection) throws JMSException {
        this.timeout = timeout;
        this.session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = session.createQueue(queueName);
        this.consumer = session.createConsumer(queue);
    }

    /**
     * Receives and returns <code>size</code> Strings from the queue.
     * Presumes that messages in queue are {@link TextMessage}.
     *
     * @param maxSize maximum number of messages to receive
     * @return <code>size</code> messages from the queue.
     */
    public List<String> receiveTextMessages(int maxSize) {
        List<String> messages = new ArrayList<>();
        try {
            while (messages.size() <= maxSize){
                TextMessage message = (TextMessage) consumer.receive(timeout);
                if (message == null) {
                    return messages;
                }else {
                    messages.add(message.getText());
                }
            }
        } catch (JMSException e) {
            throw new AsyncException(e);
        }
        return messages;
    }

    /**
     * Commits a current transaction. This acknowledges that all messages have been processed.
     * All messages received before this call will be removed from queue and not delivered again.
     */
    public void commit(){
        try {
            session.commit();
        } catch (JMSException e) {
            throw new AsyncException(e);
        }
    }

    /**
     * Rolls back current transaction. All "received" messages will be re-delivered.
     */
    public void rollback(){
        try {
            session.rollback();
        } catch (JMSException e) {
            throw new AsyncException(e);
        }
    }

    /**
     * Closes this resource (calling it is mandatory).
     */
    @Override
    public void close(){
        Util.closeQuietly(session);
    }
}
