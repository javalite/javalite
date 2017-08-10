package org.javalite.async;

import org.javalite.common.Util;

import javax.jms.*;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author igor on 8/8/17.
 */
public class BatchReceiver implements Closeable {

    private Queue queue;
    private long timeout;
    private Session session;


    protected BatchReceiver(Queue queue, long timeout, Connection connection) throws JMSException {
        this.queue = queue;
        this.timeout = timeout;
        this.session = connection.createSession(true, Session.SESSION_TRANSACTED);
    }

    /**
     * Receives and returns <code>size</code> Strings from the queue.
     * Presumes that messages in queue are {@link TextMessage}.
     *
     * @param maxSize maximum number of messages to receive
     * @return <code>size</code> messages from the queue.
     */
    public List<String> receiveTextMessages(int maxSize) {
        MessageConsumer consumer;
        List<String> messages = new ArrayList<>();
        try {
            consumer = session.createConsumer(queue);
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
