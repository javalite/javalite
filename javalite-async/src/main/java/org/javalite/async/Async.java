/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.management.ActiveMQServerControl;
import org.apache.activemq.artemis.api.core.management.QueueControl;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.JournalType;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressFullMessagePolicy;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.javalite.common.JsonHelper;
import org.javalite.common.Util;
import org.javalite.common.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Queue;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.util.*;

import static org.apache.activemq.artemis.api.core.Message.HDR_SCHEDULED_DELIVERY_TIME;
import static org.javalite.common.Collections.map;
import static org.javalite.common.Util.closeQuietly;

/**
 * Wrapper for embedded Apache ActiveMQ Artemis. It is an embedded in-memory
 * JMS server for asynchronous processing. JavaLite Async can be used in standalone applications,
 * but specifically useful in web apps for processing asynchronous jobs without delaying
 * rendering web responses.
 *
 * It sets many configuration parameters of Artemis {@link org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ} to
 * sensible values so you do not have to.
 *
 * This class also implements a Command Pattern for ease of writing asynchronous code.
 *
 * @author Igor Polevoy on 3/4/15.
 */
public class Async {

    private static final Logger LOGGER = LoggerFactory.getLogger(Async.class);
    private static final int MIN_LARGE_MESSAGE_SIZE = 819200;
    private static final String QUEUE_NAMESPACE = "queue/";

    private Injector injector;
    private final Configuration config = new ConfigurationImpl();
    private Connection consumerConnection;
    private Connection producerConnection;
    private EmbeddedActiveMQ artemisServer;
    private boolean binaryMode;

    private List<QueueConfig> queueConfigsList = new ArrayList<>();
    private boolean started;
    private InitialContext initialContext;

    /**
     * sessions that are used by listeners
     */
    private List<Session> listenerSessions = new ArrayList<>();

    /**
     * consumers that are used by listeners
     */
    private List<MessageConsumer> listenerConsumers = new ArrayList<>();


    private SessionPool senderSessionPool;
    private SessionPool receiverSessionPool;

    /**
     * Creates and configures a new instance.
     *
     * @param dataDirectory root directory where persistent messages are stored
     * @param useLibAio true to use libaio, false not to use (See Artemis log statements to check if it was detected).
     *
     * @param queueConfigs vararg of QueueConfig instances.
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

            Collections.addAll(queueConfigsList, queueConfigs);
            configureLocations(dataDirectory);
            configureAcceptor();
            configurePaging();
            configureQueues(queueConfigs);
            configureJournal(useLibAio);
            config.setThreadPoolMaxSize(-1);
            config.setGracefulShutdownEnabled(true);
            config.setScheduledThreadPoolMaxSize(10);
        } catch (AsyncException e) {
            throw e;
        } catch (Exception e) {
            throw new AsyncException("Failed to start EmbeddedJMS", e);
        }
    }

    private void configureJournal(boolean useLibAio){
        config.setJournalType(useLibAio ? JournalType.ASYNCIO : JournalType.NIO);
        config.setJournalBufferSize_AIO(MIN_LARGE_MESSAGE_SIZE);
        config.setJournalBufferSize_NIO(MIN_LARGE_MESSAGE_SIZE);
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


    private void configurePaging() {
        AddressSettings addressSettings = new AddressSettings();
        addressSettings.setAddressFullMessagePolicy(AddressFullMessagePolicy.PAGE);
        addressSettings.setMaxSizeBytes(30 * 1024 * 1024L);
        addressSettings.setPageSizeBytes(10 * 1024 * 1024);
        addressSettings.setPageCacheMaxSize(20);
        config.getAddressesSettings().put("jms.queue.*", addressSettings);
    }

    /**
     * Starts the server.
     */
    public void start(){

        try {
            artemisServer = new EmbeddedActiveMQ();
            artemisServer.setConfiguration(config);
            artemisServer.start();

            //somehow this only works after start of the server, lol.
            artemisServer.getActiveMQServer().getAddressSettingsRepository()
                    .addMatch("#", new AddressSettings()
                    .setAutoCreateQueues(false)
                    .setAutoCreateAddresses(false)
                    .setAutoDeleteQueues(false)
                    .setAutoDeleteAddresses(false));

            Wait.waitFor(() -> artemisServer.getActiveMQServer().isStarted());

            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://0");

            consumerConnection = connectionFactory.createConnection();
            receiverSessionPool = new SessionPool("Consumer", consumerConnection);

            producerConnection = connectionFactory.createConnection();
            senderSessionPool = new SessionPool("Producer", producerConnection);
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
        started = false;
        senderSessionPool.close();
        receiverSessionPool.close();
        listenerConsumers.forEach(Util::closeQuietly);
        listenerSessions.forEach(Util::closeQuietly);

        closeQuietly(producerConnection);
        closeQuietly(consumerConnection);

        try {
            ActiveMQServerControl control = artemisServer.getActiveMQServer().getActiveMQServerControl();
            String[] remoteAddresses = control.listRemoteAddresses();
            for (String address : remoteAddresses) {
                control.closeConnectionsForAddress(address);
            }
        } catch (Exception e) {
            LOGGER.warn("exception trying to close remote connections.", e);
        }
        try {
            artemisServer.stop();

//            TimeUnit.SECONDS.sleep(20);
//            Wait.waitFor(() -> !artemisServer.getActiveMQServer().isActive());

        } catch (Exception e) {
            LOGGER.warn("exception trying to stop broker.", e);
        }
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


    private void configureQueues(QueueConfig... queueConfigs) throws NamingException {

        Hashtable<String, String> jndi = new Hashtable<>();
        jndi.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");

        for (QueueConfig queueConfig : queueConfigs) {
            CoreQueueConfiguration coreQueueConfiguration = new CoreQueueConfiguration();
            coreQueueConfiguration
                    .setName(queueConfig.getName())
                    .setDurable(queueConfig.isDurable())
                    .setAddress(queueConfig.getName()).
                    setRoutingType(RoutingType.ANYCAST);
            config.addQueueConfiguration(coreQueueConfiguration);

            //# queue.[jndiName] = [physicalName]
            jndi.put("queue.queue/" + queueConfig.getName(), queueConfig.getName());
        }

        initialContext = new InitialContext(jndi);
    }

    private void configureListeners(Injector injector, List<QueueConfig> queueConfigs) throws JMSException{
        for (QueueConfig queueConfig : queueConfigs) {

            CommandListener listener = queueConfig.getCommandListener();
            if(listener != null){
                listener.setInjector(injector);
                if(injector != null){
                    injector.injectMembers(listener);
                }

                for (int i = 0; i < queueConfig.getListenerCount(); i++) {
                    Session session = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    Queue queue = session.createQueue(queueConfig.getName());
                    MessageConsumer consumer = session.createConsumer(queue);
                    consumer.setMessageListener(listener);
                    listenerSessions.add(session);
                    listenerConsumers.add(consumer);
                }
            }
        }
        consumerConnection.start();
    }

    ///******* PUBLIC METHODS BELOW ***********///


    /**
     * If true, uses binary mode to send messages. If set to false (default), will send messages
     * as strings. Test which method is faster in your environment for your CPU and IO performance.
     * generally, binary mode will use a lot less IO, but more CPU and vice versa.
     *
     * @param binaryMode true to send messages in binary mode, false to send as strings.
     */
    public void setBinaryMode(boolean binaryMode) {
        this.binaryMode = binaryMode;
    }

    /**
     * Call this method once after a constructor in order to create a Netty instance to accept out of VM messages.
     *
     * @param host host to bind to
     * @param port port to listen on
     */
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
     * @param command  command instance.
     * @param deliveryTime delivery time in milliseconds
     */
    public void send(String queueName, Command command, long deliveryTime) {
        send(queueName, command, DeliveryMode.NON_PERSISTENT, 4, 0, deliveryTime);
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
    public void send(String queueName, Command command, int deliveryMode, int priority, int timeToLive){
        send(queueName, command, deliveryMode, priority, timeToLive, -1L);
    }

    /**
     * Sends a command into a queue for processing
     *
     * @param queueName name of queue
     * @param command command to process
     * @param deliveryMode delivery mode: {@link javax.jms.DeliveryMode}.
     * @param deliveryTime delivery time in milliseconds
     */
    public void send(String queueName, Command command, int deliveryMode, long deliveryTime) {
        send(queueName, command, deliveryMode, 4, 0, deliveryTime);
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
    public void send(String queueName, Command command, int deliveryMode, int priority, long timeToLive) {
        send(queueName, command, deliveryMode, priority, timeToLive, -1);
    }

    /**
     * Sends a command into a queue for processing
     *
     * @param queueName name of queue
     * @param command command to process
     * @param deliveryMode delivery mode: {@link javax.jms.DeliveryMode}. 1 for non-persistent, 2 for persistent.
     * @param priority priority of the message. Correct values are from 0 to 9, with higher number denoting a
     *                 higher priority.
     * @param timeToLive the message's lifetime (in milliseconds, where 0 is to never expire)
     * @param deliveryTime The specified value must be a positive long corresponding to the time
     *                     the message must be delivered (in milliseconds). For instance, <code>System.currentTimeMillis() + 5000</code>
     *                     would be 5 seconds from now.
     */
    public void send(String queueName, Command command, int deliveryMode, int priority, long timeToLive, long deliveryTime) {

        checkStarted();

        long now  = System.currentTimeMillis();
        try(Session session = senderSessionPool.getSession()) {
            checkInRange(deliveryMode, 1, 2, "delivery mode");
            checkInRange(priority, 0, 9, "priority");
            if (timeToLive < 0)
                throw new AsyncException("time to live cannot be negative");

            Queue queue = (Queue) initialContext.lookup(QUEUE_NAMESPACE + queueName);

            if (queue == null)
                throw new AsyncException("Failed to find queue: " + queueName);

            Message message;
            if(binaryMode){
                BytesMessage msg = session.createBytesMessage();
                msg.writeBytes(command.toBytes());
                message = msg;
            }else{
                message = session.createTextMessage(command.toXml());
            }

            if (deliveryTime > 0) {
                message.setLongProperty(HDR_SCHEDULED_DELIVERY_TIME.toString(), deliveryTime);
            }

            try(MessageProducer producer = session.createProducer(queue);) {
                producer.send(message, deliveryMode, priority, timeToLive);
            }

        } catch (AsyncException e) {
            throw e;
        } catch (Exception e) {
            throw new AsyncException("Failed to send message", e);
        }finally {
            LOGGER.debug(JsonHelper.toJsonString(map("message", "completed sending command", "time_millis", now - System.currentTimeMillis(), "command", command.getClass(), "queue", queueName)));
        }
    }




    /**
     * Receives a command from a queue synchronously. If this queue also has listeners, then commands will be distributed across
     * all consumers.
     *
     * @param queueName name of queue
     * @return command if found. If command not found, this method will block till a command is present in queue.
     *
     * see {@link #receiveCommand(String, long)}
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
     * see {@link #receiveCommand(String, long)}
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
     * see {@link #receiveCommand(String, long)}
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

        try {
            Message message = receiveMessage(queueName, timeout);
            if(message == null){
                return null;
            }else{
                Command command;
                if(binaryMode){
                    command = Command.fromBytes(getBytes((BytesMessage) message));
                }else {
                    command = Command.fromXml(((TextMessage)message).getText());
                }
                command.setJMSMessageID(message.getJMSMessageID());
                return command;
            }
        } catch (Exception e) {
            throw new AsyncException("Could not get command", e);
        }
    }


    /**
     * Receives a messafge from a queue asynchronously.If this queue also has listeners, then messages will be distributed across
     * all consumers.
     *
     * @param queueName name of queue
     * @param timeout timeout in millis.
     *
     * @return message if  found, null if not.
     */
    public Message receiveMessage(String queueName, long timeout) {
        checkStarted();
        try(Session session = receiverSessionPool.getSession()){
            try(MessageConsumer consumer = session.createConsumer(lookupQueue(queueName))) {
                return consumer.receive(timeout);
            }
        } catch (Exception e) {
            throw new AsyncException("Could not get message", e);
        }
    }

    /**
     * Sends a non-expiring {@link TextMessage} with average priority.
     *
     * @param queueName name of queue
     * @param text body of message
     */
    public void sendTextMessage(String queueName, String text){
        sendTextMessage(queueName, text, DeliveryMode.NON_PERSISTENT, 4, 0);
    }

    /**
     * Sends a {@link TextMessage}.
     *
     * @param queueName name of queue
     * @param text body of message
     * @param deliveryMode delivery mode: {@link javax.jms.DeliveryMode}.
     * @param priority priority of the message. Correct values are from 0 to 9, with higher number denoting a
     *                 higher priority.
     * @param timeToLive the message's lifetime (in milliseconds, where 0 is to never expire)
     */
    public void sendTextMessage(String queueName, String text, int deliveryMode, int priority, int timeToLive) {
        checkStarted();
        long now  = System.currentTimeMillis();
        try(Session session = producerConnection.createSession()) {
            checkInRange(deliveryMode, 1, 2, "delivery mode");
            checkInRange(priority, 0, 9, "priority");
            if (timeToLive < 0)
                throw new AsyncException("time to live cannot be negative");
            Message message = session.createTextMessage(text);

            MessageProducer p = session.createProducer(lookupQueue(queueName));
            p.send(message, deliveryMode, priority, timeToLive);
        } catch (AsyncException e) {
            throw e;
        } catch (Exception e) {
            throw new AsyncException("Failed to send message", e);
        }finally {
            LOGGER.debug(JsonHelper.toJsonString(map("message", "completed sending text message", "time_millis", now - System.currentTimeMillis(), "queue", queueName)));
        }

    }

    /**
     * Generate a {@link BatchReceiver} to receive and process stored messages.
     * This method ALWAYS works in the context of a transaction.
     *
     * @param queueName name of queue
     * @param timeout timeout to wait.
     * @return instance  of {@link BatchReceiver}.
     */
    public BatchReceiver getBatchReceiver(String queueName, long timeout){
        try {
            return new BatchReceiver(queueName, timeout, consumerConnection);
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }

    /**
     * Returns top commands in queue. Does not remove anything from queue. This method can be used for
     * an admin tool to peek inside the queue.
     *
     * @param count number of commands to lookup.
     * @return top commands in queue or empty list is nothing is found in queue.
     */
    public List<Command> getTopCommands(int count, String queueName)  {
        checkStarted();
        List<Command> res = new ArrayList<>();

        try(Session session = consumerConnection.createSession()) {

            Enumeration messages = session.createBrowser(lookupQueue(queueName)).getEnumeration();
            for(int i = 0; i < count && messages.hasMoreElements(); i++) {
                Command command;
                Message message = (Message) messages.nextElement();
                if(binaryMode){
                    command = Command.fromBytes(getBytes((BytesMessage) message));
                }else{
                    command = Command.fromXml(((TextMessage)message).getText());
                }
                command.setJMSMessageID(message.getJMSMessageID());
                res.add(command);
            }
            return res;
        } catch (Exception e) {
            throw new AsyncException("Could not lookup commands", e);
        }
    }

    /**
     * Returns top <code>TextMessage</code>s in queue. Does not remove anything from queue. This method can be used for
     * an admin tool to peek inside the queue.
     *
     * @param maxSize max number of messages to lookup.
     * @return top commands in queue or empty list is nothing is found in queue.
     */
    public List<String> getTopTextMessages(int maxSize, String queueName)  {
        checkStarted();
        List<String> res = new ArrayList<>();
        try(Session session = consumerConnection.createSession()) {
            Enumeration messages = session.createBrowser(lookupQueue(queueName)).getEnumeration();
            for(int i = 0; i < maxSize && messages.hasMoreElements(); i++) {
                TextMessage message = (TextMessage) messages.nextElement();
                res.add(message.getText());
            }
            return res;
        } catch (Exception e) {
            throw new AsyncException("Could not lookup messages", e);
        }
    }

    private Queue lookupQueue(String queueName) throws NamingException {
        return (Queue) initialContext.lookup(QUEUE_NAMESPACE + queueName);
    }

    /**
     * This method exists for testing. Uses browser to retrieve the message first in queue to deliver.
     */
    Message lookupMessage(String queueName)  {
        checkStarted();

        try(Session session = consumerConnection.createSession()) {
            Enumeration messages = session.createBrowser(lookupQueue(queueName)).getEnumeration();
            return (Message) messages.nextElement();
        }catch (NoSuchElementException e){
            return null;
        }
        catch (Exception e) {
            throw new AsyncException("Lookup failed", e);
        }
    }

    public static byte[] getBytes(BytesMessage message) throws JMSException {
        //ok to cast long to int; do not expect gigantic messages
        int len = (int)message.getBodyLength();
        byte[] bytes = new byte[len];
        message.readBytes(bytes, len);
        return bytes;
    }

    private QueueControl getQueueControl(String queueName) {
        checkStarted();
        for (Object resource : artemisServer.getActiveMQServer().getManagementService().getResources(QueueControl.class)) {

            if(resource instanceof QueueControl && ((QueueControl)resource).getName().equals(queueName)){
                return (QueueControl) resource;
            }
        }
        throw  new AsyncException("Failed to find queue: " + queueName);
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
        }
        catch (Exception e) {
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
     * Moves all messages from one queue to another
     *
     * @param source name of source queue
     * @param target name of target queue
     *
     * @return number of messages moved
     */
    public int moveMessages(String source, String target){

        try {
            return getQueueControl(source).moveMessages("", target);
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }

    /**
     * Moves a message from one queue to another
     *
     * @param jmsMessageId JMS message id of a message to move
     * @param source name of source queue
     * @param target name of target queue
     *
     * @return true if message moved
     */
    public boolean moveMessage(String jmsMessageId, String source, String target){
        try {
            //see: https://stackoverflow.com/questions/45871689/apache-artemis-how-to-move-jms-messages-to-a-different-queue/45883024
            return getQueueControl(source).moveMessages("AMQUserID='" + jmsMessageId + "'", target) == 1;
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }

    /**
     * Get additional server configuration.
     */
    public Configuration getConfig() {
        return config;
    }
}
