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


package org.javalite.hornet_nest;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.api.jms.management.JMSQueueControl;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.settings.impl.AddressFullMessagePolicy;
import org.hornetq.core.settings.impl.AddressSettings;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.embedded.EmbeddedJMS;

import javax.jms.*;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.javalite.hornet_nest.NestUtil.message2Command;

/**
 * Wrapper for embedded HornetQ. It is an embedded in-memory
 * JMS server for asynchronous processing. It can be used in standalone applications,
 * but specifically useful in web apps for processing asynchronous jobs without delaying
 * web responses.
 *
 * It sets many configuration parameters of HornetQ {@link org.hornetq.jms.server.embedded.EmbeddedJMS} to
 * sensible values so you do not have to.
 *
 * This class also implements a Command Pattern for ease of writing asynchronous code.
 *
 * @author Igor Polevoy on 3/4/15.
 */
public class HornetNest {

    private final Configuration config;
    private final JMSConfiguration jmsConfig;
    private Connection consumerConnection;
    private Connection producerConnection;
    private EmbeddedJMS jmsServer;
    private QueueConfig[] queueConfigs;
    private boolean configured = false;

    /**
     * Creates and configures a new instance.
     *
     * @param dataDirectory root directory where persistent messages are stored
     * @param useLibAio true to use libaio, false if not installed.
     *                  See <a href="http://docs.jboss.org/hornetq/2.2.5.Final/user-manual/en/html/libaio.html">Libaio Native Libraries</a>
     * @param queueConfigs vararg of QueueConfig> instances.
     */
    public HornetNest(String dataDirectory, boolean useLibAio, QueueConfig... queueConfigs) {
        jmsServer = new EmbeddedJMS();
        config = new ConfigurationImpl();
        jmsConfig = new JMSConfigurationImpl();

        try {
            configureLocations(dataDirectory);
            configureAcceptor();
            configureConnectionFactory();
            configurePaging();
            config.setJournalType(useLibAio ? JournalType.ASYNCIO : JournalType.NIO);
            configureQueues(queueConfigs);

            config.setThreadPoolMaxSize(-1);
            config.setScheduledThreadPoolMaxSize(10);
            jmsServer.setConfiguration(config);
            jmsServer.setJmsConfiguration(jmsConfig);
            jmsServer.start();

            ConnectionFactory connectionFactory = (ConnectionFactory) jmsServer.lookup("/cf");
            consumerConnection = connectionFactory.createConnection();
            producerConnection = connectionFactory.createConnection();

            configureListeners(queueConfigs);
        } catch (Exception e) {
            throw new HornetNestException("Failed to start EmbeddedJMS", e);
        }
    }


    private void configureLocations(String dataDirectory) {
        if (dataDirectory == null || !new File(dataDirectory).exists()) {
            throw new HornetNestException("Must provide data directory that exists");
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
        ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl("cf", false, singletonList("connector"), "/cf");
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

    private void checkRange(int value, int min, int max, String name) {
        if (value < min || value > max) {
            throw new HornetNestException("incorrect " + name + " value");
        }
    }

    private void configureQueues(QueueConfig... queueConfigs) throws JMSException, IllegalAccessException, InstantiationException {
        for (QueueConfig queueConfig : queueConfigs) {
            jmsConfig.getQueueConfigurations().add(new JMSQueueConfigurationImpl(queueConfig.getName(), null, true,
                    "/queue/" + queueConfig.getName()));
        }
    }

    private void configureListeners(QueueConfig... queueConfigs) throws JMSException, IllegalAccessException, InstantiationException {
        for (QueueConfig queueConfig : queueConfigs) {
            Queue queue = (Queue) jmsServer.lookup("/queue/" + queueConfig.getName());
            for (int i = 0; i < queueConfig.getListenerCount(); i++) {
                CommandListener listener = (CommandListener) queueConfig.getCommandListenerClass().newInstance();
                Session session = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                session.createConsumer(queue).setMessageListener(listener);
            }
        }
        consumerConnection.start();
    }

    ///******* PUBLIC METHODS BELOW ***********///


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
        try {
            checkRange(deliveryMode, 1, 2, "delivery mode");
            checkRange(priority, 0, 9, "priority");
            if (timeToLive < 0)
                throw new HornetNestException("time to live cannot be negative");

            Queue queue = (Queue) jmsServer.lookup("/queue/" + queueName);
            if (queue == null)
                throw new HornetNestException("Failed to find queue: " + queueName);

            Session session = producerConnection.createSession();
            TextMessage msg = session.createTextMessage(command.toString());
            msg.setStringProperty("command_class", command.getClass().getName());
            MessageProducer p = session.createProducer(queue);
            p.send(msg, deliveryMode, priority, timeToLive);
        } catch (HornetNestException e) {
            throw e;
        } catch (Exception e) {
            throw new HornetNestException("Failed to send message", e);
        }
    }


    /**
     * Stops this JMS server.
     */
    public void stop() {
        try {
            consumerConnection.close();
        } catch (Exception e) {}
        try {
            producerConnection.close();
        } catch (Exception e) {}

        try {
            jmsServer.stop();
        } catch (Exception e) {}
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
     * all consumers.
     *
     * @param queueName name of queue
     * @param timeout timeout in milliseconds. If a command is not received during a timeout, this methods returns null.
     *
     * @return command if found. If command not found, this method will block till a command is present in queue or a timeout expires.
     */
    public Command receiveCommand(String queueName, long timeout) {
        Session session = null;
        try{
            session = consumerConnection.createSession();
            Queue queue = (Queue) jmsServer.lookup("/queue/" + queueName);
            MessageConsumer consumer = session.createConsumer(queue);
            return message2Command((TextMessage) consumer.receive(timeout));
        } catch (Exception e) {
            throw new HornetNestException("Could not get command", e);
        }finally {
            try{ if(session != null)session.close(); }catch(Throwable e){}
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
        List<Command> res = new ArrayList<>();
        Session session = null;
        try {
            session = consumerConnection.createSession();
            Queue queue = (Queue) jmsServer.lookup("/queue/" + queueName);
            Enumeration messages = session.createBrowser(queue).getEnumeration();
            for(int i = 0; i < count && messages.hasMoreElements(); i++) {
                TextMessage msg = (TextMessage)messages.nextElement();
                res.add(message2Command(msg));
            }
            return res;
        } catch (Exception e) {
            throw new HornetNestException("Could not lookup commands", e);
        } finally {
            try{ if(session != null)session.close(); }catch(Throwable e){}
        }
    }

    private JMSQueueControl getQueueControl(String queue) {
        ObjectName queueName;
        try {
            queueName = ObjectNameBuilder.DEFAULT.getJMSQueueObjectName(queue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return MBeanServerInvocationHandler.newProxyInstance(ManagementFactory.getPlatformMBeanServer(),
                queueName, JMSQueueControl.class, false);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
