/*
Copyright 2009-2016 Igor Polevoy

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
import org.javalite.common.JsonHelper;
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
        try {
            Command command = parseCommand(message);
            command.setJMSMessageID(message.getJMSMessageID());
            if (injector != null) {
                injector.injectMembers(command);
            }
            long start = System.currentTimeMillis();
            onCommand(command);

            LOGGER.info(JsonHelper.toJsonString(map("processed_millis", (System.currentTimeMillis() - start), "info", command.getParams())));
        } catch (Exception e) {
            throw new AsyncException("Failed to process command", e);
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
