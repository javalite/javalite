package org.javalite.async;

import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Serves as a pool of sessions for local sending/receiving to/from the Async.
 *
 * @author igor on 11/2/17.
 */
class SessionPool {

    private Logger LOGGER;

    private static final int MAX_AGE_MILLIS = 30000;

    private final ConcurrentLinkedQueue<PooledSession> sessions = new ConcurrentLinkedQueue<>();
    private Connection producerConnection;
    private boolean closed = false;
    private SessionCleaner sessionCleaner = new SessionCleaner();


    protected SessionPool(String name, Connection producerConnection) {
        this.producerConnection = producerConnection;
        LOGGER = LoggerFactory.getLogger("SessionPool: " + name);
        sessionCleaner.start();

    }

    protected Session getSession() throws JMSException {
        if (closed) {
            throw new AsyncException("pool already closed!");
        }
        Session session;
        synchronized (sessions){
            session = sessions.poll();
        }

        if (session != null) {
            LOGGER.debug("Found existing session: " + session + ". Pool size: " + sessions.size());
            return session;
        } else {
            session = createNewSession();
            LOGGER.debug("Created new session: " + session + ". Pool size: " + sessions.size());
            return session;
        }
    }

    /**
     * Closes all underlying JMS sessions.
     */
    protected synchronized void close() {
        closed = true;
        sessionCleaner.close();
        sessions.stream().forEach(PooledSession::reallyClose);
    }


    protected void reclaim(PooledSession session) {
        session.markLastUsedTime();
        sessions.add(session);
        LOGGER.debug("Reclaimed session: " + session);
    }

    private Session createNewSession() throws JMSException {
        return new PooledSession(producerConnection.createSession(), this);
    }


    /**
     * Watcher thread. Triggers once per minute and if finds sessions that  have not been used in more than a minute,
     * closes all.
     */
    private class SessionCleaner{
        private final ScheduledExecutorService cleanupExecutor = newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("SessionCleaner Thread");
            thread.setDaemon(true);
            return thread;
        });

        private Runnable command = () -> {
            synchronized (sessions){
                if(sessions.size() > 0){
                    long ageMillis = System.currentTimeMillis() - sessions.peek().getLastUsed();
                    LOGGER.info("Checking stale sessions...");
                    if(ageMillis > MAX_AGE_MILLIS){
                        LOGGER.info("Found sessions are older than " + MAX_AGE_MILLIS / 1000 + " seconds, closing...");
                        for(PooledSession session = sessions.poll(); session != null;  session = sessions.poll()){
                            session.reallyClose();
                            LOGGER.info("Closed session: " + session);
                        }
                    }
                }
            }
        };
        protected void close(){
            cleanupExecutor.shutdown();
        }

        protected synchronized void start(){
            cleanupExecutor.scheduleAtFixedRate(command, 30, 30, TimeUnit.SECONDS);
            LOGGER.info("Starting to clean stale sessions...");
        }
    }
}
