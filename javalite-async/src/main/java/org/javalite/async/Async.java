/*
Copyright 2009-2015 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package org.javalite.async;

import com.google.inject.Injector;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.JournalType;
import org.apache.activemq.artemis.core.settings.impl.AddressFullMessagePolicy;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.jms.Queue;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.javalite.common.Collections.map;
import static org.javalite.common.Util.closeQuietly;

/**
 * Wrapper for embedded Apache ActiveMQ Artemis. It is an embedded in-memory
 * JMS server for asynchronous processing. JavaLite Async can be used in standalone applications,
 * but specifically useful in web apps for processing asynchronous jobs without delaying
 * rendering web responses.
 *
 * It sets many configuration parameters of Artemis {@link org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS} to
 * sensible values so you do not have to.
 *
 * This class also implements a Command Pattern for ease of writing asynchronous code.
 *
 * @author Igor Polevoy on 3/4/15.
 */
public class Async {

    private final static Logger LOGGER = LoggerFactory.getLogger("JavaLite Async");

    private final static int MIN_LARGE_MESSAGE_SIZE = 100 * 4096;

    private static final String QUEUE_NAMESPACE = "/queue/";
    private Injector injector;
    private final Configuration config;
    private final JMSConfiguration jmsConfig;
    private Connection consumerConnection;
    private Connection producerConnection;
    private EmbeddedJMS jmsServer;

    private List<MessageConsumer> messageConsumers = new ArrayList<>();
    private List<Session> sessions = new ArrayList<>();
    private List<QueueConfig> queueConfigsList = new ArrayList<>();
    private boolean started = false;

    /**
     * Creates and configures a new instance.
     *
     * @param dataDirectory root directory where persistent messages are stored
     * @param useLibAio true to use libaio, false if not installed.
     *
     * @param queueConfigs vararg of QueueConfig> instances.
     */

    public Async(String dataDirectory, boolean useLibAio, QueueConfig... queueConfigs) {
        this(dataDirectory, useLibAio, null, queueConfigs);
    }

    /**
     * Creates and configures a new instance.
     *
     * @param dataDirectory root directory where persistent messages are stored
     * @param useLibAio true to use libaio, false to use NIO.
     * @param  injector Google Guice injector. Used to inject dependency members into commands if needed.
     * @param queueConfigs vararg of QueueConfig> instances.
     */
    public Async(String dataDirectory, boolean useLibAio, Injector injector, QueueConfig... queueConfigs) {


        try {
            this.injector = injector;
            jmsServer = new EmbeddedJMS();
            config = new ConfigurationImpl();
            jmsConfig = new JMSConfigurationImpl();
            Collections.addAll(queueConfigsList, queueConfigs);
            configureLocations(dataDirectory);
            configureAcceptor();
            configureConnectionFactory();
            configurePaging();
            configureQueues(queueConfigs);
            configureJournal(useLibAio);
            config.setThreadPoolMaxSize(-1);
            config.setScheduledThreadPoolMaxSize(10);

        } catch (AsyncException e) {
            throw e;
        } catch (Exception e) {
            throw new AsyncException("Failed to start EmbeddedJMS", e);
        }
    }

    private void configureJournal(boolean useLibAio){
        config.setJournalType(useLibAio ? JournalType.ASYNCIO : JournalType.NIO);
        config.setJournalBufferSize_AIO(2 * MIN_LARGE_MESSAGE_SIZE);
        config.setJournalBufferSize_NIO(2 * MIN_LARGE_MESSAGE_SIZE);
    }

    private void configureLocations(String dataDirectory) {
        if (dataDirectory == null || !new File(dataDirectory).exists()) {
            throw new AsyncException("Must provide data directory that exists");
        }
        config.setBindingsDirectory(dataDirectory + "/bindings");
        config.setJournalDirectory(dataDirectory + "/journal");
        config.setLargeMessagesDirectory(dataDirectory + "/largemessages");
        config.setPagingDirectory(dataDirectory + "/paging");
    }

    private void configureAcceptor() {
        config.setPersistenceEnabled(true);
        config.setSecurityEnabled(false);
        config.getAcceptorConfigurations().add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
        config.getConnectorConfigurations().put("connector", new TransportConfiguration(InVMConnectorFactory.class.getName()));
    }

    private void configureConnectionFactory() {

        ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl();
        cfConfig.setName("cf").setConnectorNames(singletonList("connector")).setBindings("/cf");

        /*see https://community.jboss.org/thread/160367
                Re: Connection timeout issues - Connection failure has been detected
                In my case, below configuration (made on JBoss 7.1.1.Final) has helped:
                <client-failure-check-period>2147483646</client-failure-check-period>
                <connection-ttl>-1</connection-ttl>
                <reconnect-attempts>-1</reconnect-attempts>
               */

        cfConfig.setClientFailureCheckPeriod(Long.MAX_VALUE);
        cfConfig.setConnectionTTL(-1);
        cfConfig.setReconnectAttempts(-1);
        cfConfig.setCompressLargeMessages(true);
        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);
    }

    private void configurePaging() {
        AddressSettings addressSettings = new AddressSettings();
        addressSettings.setAddressFullMessagePolicy(AddressFullMessagePolicy.PAGE);
        addressSettings.setMaxSizeBytes(30 * 1024 * 1024);
        addressSettings.setPageSizeBytes(10 * 1024 * 1024);
        addressSettings.setPageCacheMaxSize(20);
        config.getAddressesSettings().put("jms.queue.*", addressSettings);
    }

    private void checkInRange(int value, int min, int max, String name) {
        if (value < min || value > max) {
            throw new AsyncException("incorrect " + name + " value");
        }
    }

    private void checkStarted(){
        if(!started){
            throw new AsyncException("Server not started.");
        }
    }

    private void configureQueues(QueueConfig... queueConfigs) throws JMSException, IllegalAccessException, InstantiationException {
        for (QueueConfig queueConfig : queueConfigs) {
            JMSQueueConfigurationImpl configuration = new JMSQueueConfigurationImpl();
            configuration.setName(queueConfig.getName()).setSelector("").setDurable(queueConfig.isDurable()).setBindings(QUEUE_NAMESPACE + queueConfig.getName());
            jmsConfig.getQueueConfigurations().add(configuration);
        }
    }

    private void configureListeners(Injector injector, List<QueueConfig> queueConfigs) throws JMSException, IllegalAccessException, InstantiationException {
        for (QueueConfig queueConfig : queueConfigs) {
            Queue queue = (Queue) jmsServer.lookup(QUEUE_NAMESPACE + queueConfig.getName());
            for (int i = 0; i < queueConfig.getListenerCount(); i++) {
                CommandListener listener = (CommandListener) queueConfig.getCommandListenerClass().newInstance();
                listener.setInjector(injector);
                if(injector != null){
                    injector.injectMembers(listener);
                }
                Session session = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageConsumer consumer = session.createConsumer(queue);
                consumer.setMessageListener(listener);
                sessions.add(session);
                messageConsumers.add(consumer);
            }
        }
        consumerConnection.start();
    }

    ///******* PUBLIC METHODS BELOW ***********///

    public void configureNetty(String host, int port){
        Map<String, Object> params = map(TransportConstants.HOST_PROP_NAME, host, TransportConstants.PORT_PROP_NAME, port);
        config.getAcceptorConfigurations().add(new TransportConfiguration(NettyAcceptorFactory.class.getName(), params));
    }

    /**
     * Sends a command into a queue for processing
     *
     * @param queueName name of queue
     * @param command  command instance.
     */
    public void send(String queueName, Command command) {
        send(queueName, command, DeliveryMode.NON_PERSISTENT, 4, 0);
    }

    /**
     * Sends a command into a queue for processing
     *
     * @param queueName name of queue
     * @param command command to process
     * @param deliveryMode delivery mode: {@link javax.jms.DeliveryMode}.
     */
    public void send(String queueName, Command command, int deliveryMode) {
        send(queueName, command, deliveryMode, 4, 0);
    }

    /**
     * Sends a command into a queue for processing
     *
     * @param queueName name of queue
     * @param command command to process
     * @param deliveryMode delivery mode: {@link javax.jms.DeliveryMode}.
     * @param priority priority of the message. Correct values are from 0 to 9, with higher number denoting a
     *                 higher priority.
     * @param timeToLive the message's lifetime (in milliseconds, where 0 is to never expire)
     */
    public void send(String queueName, Command command, int deliveryMode, int priority, int timeToLive) {
        checkStarted();

        try(Session session = producerConnection.createSession()) {
            checkInRange(deliveryMode, 1, 2, "delivery mode");
            checkInRange(priority, 0, 9, "priority");
            if (timeToLive < 0)
                throw new AsyncException("time to live cannot be negative");

            Queue queue = (Queue) jmsServer.lookup(QUEUE_NAMESPACE + queueName);
            if (queue == null)
                throw new AsyncException("Failed to find queue: " + queueName);

            TextMessage msg = session.createTextMessage(command.toXml());
            MessageProducer p = session.createProducer(queue);
            p.send(msg, deliveryMode, priority, timeToLive);
        } catch (AsyncException e) {
            throw e;
        } catch (Exception e) {
            throw new AsyncException("Failed to send message", e);
        }
    }

    /**
     * Starts the server.
     */
    public void start(){

        try {
            jmsServer.setConfiguration(config);
            jmsServer.setJmsConfiguration(jmsConfig);

            jmsServer.start();

            ConnectionFactory connectionFactory = (ConnectionFactory) jmsServer.lookup("/cf");
            if(connectionFactory == null){
                throw new AsyncException("Failed to start EmbeddedJMS server due to previous errors.");
            }
            consumerConnection = connectionFactory.createConnection();
            producerConnection = connectionFactory.createConnection();

            configureListeners(injector, queueConfigsList);
            started = true;
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }

    /**
     * Stops this JMS server.
     */
    public void stop() {

        for (MessageConsumer consumer : messageConsumers) {
            closeQuietly(consumer);
        }

        for (Session session : sessions) {
            closeQuietly(session);
        }

        closeQuietly(consumerConnection);
        closeQuietly(producerConnection);

        try {
            jmsServer.stop();
        } catch (Exception e) {
            LOGGER.warn("exception trying to stop broker.", e);
        }

        started = false;
    }

    /**
     * Receives a command from a queue synchronously. If this queue also has listeners, then commands will be distributed across
     * all consumers.
     *
     * @param queueName name of queue
     * @return command if found. If command not found, this method will block till a command is present in queue.
     *
     * @see {@link #receiveCommand(String, long)}
     */
    public Command receiveCommand(String queueName) {
        return receiveCommand(queueName, 0);
    }

    /**
     * Receives a command from a queue synchronously. If this queue also has listeners, then commands will be distributed across
     * all consumers. This method will block until a command becomes available for this consumer.
     *
     * @param queueName name of queue
     * @param type expected class of a command
     * @return command if found. If command not found, this method will block till a command is present in queue.
     *
     * @see {@link #receiveCommand(String, long)}
     */
    @SuppressWarnings("unchecked")
    public <T extends Command> T receiveCommand(String queueName, Class<T> type) {
        return (T) receiveCommand(queueName, 0);
    }

    /**
     * Receives a command from a queue synchronously. If this queue also has listeners, then commands will be distributed across
     * all consumers.
     *
     * @param queueName name of queue
     * @param timeout  timeout in milliseconds. If a command is not received during a timeout, this methods returns null.
     * @param type expected class of a command
     * @return command if found. If command not found, this method will block till a command is present in queue.
     *
     * @see {@link #receiveCommand(String, long)}
     */
    @SuppressWarnings("unchecked")
    public <T extends Command> T receiveCommand(String queueName,  int timeout, Class<T> type) {
        return (T) receiveCommand(queueName, timeout);
    }


    /**
     * Receives a command from a queue synchronously. If this queue also has listeners, then commands will be distributed across
     * all consumers.
     *
     * @param queueName name of queue
     * @param timeout timeout in milliseconds. If a command is not received during a timeout, this methods returns null.
     *
     * @return command if found. If command not found, this method will block till a command is present in queue or a timeout expires.
     */
    public Command receiveCommand(String queueName, long timeout) {
        checkStarted();
        try(Session session = consumerConnection.createSession()){
            Queue queue = (Queue) jmsServer.lookup(QUEUE_NAMESPACE + queueName);
            MessageConsumer consumer = session.createConsumer(queue);
            TextMessage message = (TextMessage) consumer.receive(timeout);
            return message == null ? null : Command.fromXml(message.getText());
        } catch (Exception e) {
            throw new AsyncException("Could not get command", e);
        }
    }

    /**
     * Returns top commands in queue. Does not remove anything from queue. This method can be used for
     * an admin tool to peek inside the queue.
     *
     * @param count number of commands to lookup.
     * @return top commands in queue.
     */
    public List<Command> getTopCommands(int count, String queueName)  {
        checkStarted();
        List<Command> res = new ArrayList<>();

        try(Session session = consumerConnection.createSession()) {
            Queue queue = (Queue) jmsServer.lookup(QUEUE_NAMESPACE + queueName);
            Enumeration messages = session.createBrowser(queue).getEnumeration();
            for(int i = 0; i < count && messages.hasMoreElements(); i++) {
                TextMessage msg = (TextMessage)messages.nextElement();
                res.add(Command.fromXml(msg.getText()));
            }
            return res;
        } catch (Exception e) {
            throw new AsyncException("Could not lookup commands", e);
        }
    }

    private JMSQueueControl getQueueControl(String queue) throws Exception {
        checkStarted();
        ObjectName queueName = ObjectNameBuilder.DEFAULT.getJMSQueueObjectName(queue);
        return MBeanServerInvocationHandler.newProxyInstance(ManagementFactory.getPlatformMBeanServer(),
                queueName, JMSQueueControl.class, false);
    }

    /**
     * Returns counts of messages for all queues.
     *
     * @return map, where a key is a queue name, and value is a number of messages currently in that queue.0
     */
    public Map<String, Long> getMessageCounts(){
        Map<String, Long> counts = new HashMap<>();
        for (QueueConfig queueConfig : queueConfigsList) {
            counts.put(queueConfig.getName(), getMessageCount(queueConfig.getName()));
        }
        return counts;
    }


    /**
     * Returns number of messages currently in queue
     *
     * @param queue queue name
     * @return number of messages currently in queue
     */
    public long getMessageCount(String queue){
        try {
            return getQueueControl(queue).getMessageCount();
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }


    /**
     * Resumes a paused queue
     *
     * @param queueName queue name
     */
    public void resume(String queueName) {
        try {
            getQueueControl(queueName).resume();
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }

    /**
     * Pauses a queue. A paused queue stops delivering commands to listeners. It still can accumulate commands.
     *
     * @param queueName queue name.
     */
    public void pause(String queueName) {
        try {
            getQueueControl(queueName).pause();
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }

    /**
     * @param queueName queue name
     * @return true if queue is paused, false if not.
     */
    public boolean isPaused(String queueName) {
        try {
            return getQueueControl(queueName).isPaused();
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }

    /**
     * Removes messages from queue.
     *
     * @param queueName queue name
     * @param filter filter selector as  in JMS specification.
     *               See: <a href="http://docs.oracle.com/cd/E19798-01/821-1841/bncer/index.html">JMS Message Selectors</a>
     * @return number of messages removed
     */
    public int  removeMessages(String queueName, String filter) {
        try {
            return getQueueControl(queueName).removeMessages(filter);
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }

    /**
     * Removes all messages from queue.
     *
     * @param queueName queue name.
     * @return number of messages removed
     */
    public int removeAllMessages(String queueName) {
        try {
            return getQueueControl(queueName).removeMessages(null);
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }


    /**
     * Get additional JMS configuration.
     */
    public JMSConfiguration getJmsConfig() {
        return jmsConfig;
    }

    /**
     * Get additional server configuration.
     */
    public Configuration getConfig() {
        return config;
    }
}
