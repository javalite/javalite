package org.javalite.hornet_nest;

import javax.jms.TextMessage;

/**
 * @author Igor Polevoy on 8/11/15.
 */
public class NestUtil {
    static Command message2Command(TextMessage message){
        try {

            if(message == null){
                return null;
            }else{
                String className = message.getStringProperty("command_class");
                Command command = (Command) Class.forName(className).newInstance();
                command.fromString(message.getText());
                return command;
            }
        } catch (Exception e) {
            throw new HornetNestException("Failed to process command", e);
        }
    }
}
