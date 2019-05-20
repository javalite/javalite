package org.javalite.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Serves as a pool of sessions for local sending/receiving to/from the Async.
 *
 * @author igor on 11/2/17.
 */
public class SessionPool {

    private final Logger LOGGER;

    private static final int MAX_AGE_MILLIS = 5000;

    private final LinkedList<PooledSession> sessions = new LinkedList<>();
    private final Connection connection;
    private boolean closed = false;
    private final SessionCleaner sessionCleaner = new SessionCleaner();


    public SessionPool(String name, Connection connection) {
        this.connection = connection;
        LOGGER = LoggerFactory.getLogger("SessionPool: " + name);
        sessionCleaner.start();
    }

    public Session getSession() throws JMSException {
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
    public void close() {
        synchronized (sessions) {
            closed = true;
            sessionCleaner.close();
            sessions.stream().forEach(PooledSession::reallyClose);
        }
    }


    void reclaim(PooledSession session) {
        session.markLastUsedTime();
        synchronized (sessions){
            sessions.add(session);
        }
        LOGGER.debug("Reclaimed session: " + session);
    }

    public Session createNewSession() throws JMSException {
        return new PooledSession(connection.createSession(), this);
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
            LOGGER.debug("Checking stale sessions...");
            try {
                synchronized (sessions){
                    if (!sessions.isEmpty()) {
                        for (Iterator<PooledSession> iterator = sessions.iterator(); iterator.hasNext(); ) {
                            PooledSession session = iterator.next();
                            long ageMillis = System.currentTimeMillis() - session.getLastUsed();
                            if (ageMillis > MAX_AGE_MILLIS) {
                                session.reallyClose();
                                iterator.remove();
                                LOGGER.debug("Session is " + ageMillis + " milliseconds old, closing: " + session);
                            }else {
                                break;
                            }

                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to close sessions!", e);
            }
        };

        protected void close(){
            cleanupExecutor.shutdown();
        }

        protected void start() {
            synchronized (sessions) {
                cleanupExecutor.scheduleAtFixedRate(command, 5, 5, TimeUnit.SECONDS);
                LOGGER.debug("Starting to clean stale sessions...");
            }
        }
    }
}
