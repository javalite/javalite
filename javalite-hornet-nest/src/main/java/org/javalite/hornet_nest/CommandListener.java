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

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import static org.javalite.hornet_nest.NestUtil.message2Command;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class CommandListener implements MessageListener{

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage tm = (TextMessage) message;
            onCommand(message2Command(tm));
        } catch (Exception e) {
            throw new HornetNestException("Failed to process command", e);
        }
    }

    public <T extends Command> void onCommand(T command) {
        command.execute();
    }

}
