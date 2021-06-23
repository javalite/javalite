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
import org.javalite.json.JSONHelper;
import org.javalite.logging.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.IOException;

import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class CommandListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

    private Injector injector;

    @Override
    public void onMessage(Message message) {
        Context.clear();
        try {
            Command command = parseCommand(message);
            command.setJMSMessageID(message.getJMSMessageID());
            if (injector != null) {
                injector.injectMembers(command);
            }
            long start = System.currentTimeMillis();
            onCommand(command);
            LOGGER.info(JSONHelper.toJsonString(map("processed_millis", (System.currentTimeMillis() - start), "command", command.getClass().getSimpleName())));
        } catch (Exception e) {
            LOGGER.error("Failed to process message: {}", getCommandAsText(message), e);
            throw new AsyncException("Failed to process message", e);
        }finally {
            Context.clear();
        }
    }

    private String getCommandAsText(Message message) {
        try {
            if(message instanceof BytesMessage){
                return new String(Async.getBytes((BytesMessage) message));
            }else if(message instanceof TextMessage){
                return ((TextMessage) message).getText();
            }else {
                return "Failed to convert message to text: " + message.toString();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert message to text: " + message.toString());
            return "failed to convert message to text: " + message.toString();
        }
    }

    public <T extends Command> void onCommand(T command) {
        command.execute();
    }

    void setInjector(Injector injector) {
        this.injector = injector;
    }

    protected Command parseCommand(Message message) throws IOException, JMSException {
        Command command;
        if (message instanceof TextMessage) {
            command = Command.fromXml(((TextMessage) message).getText());
        } else {
            byte[] bytes = Async.getBytes((BytesMessage) message);
            command = Command.fromBytes(bytes);
        }
        return command;
    }
}
