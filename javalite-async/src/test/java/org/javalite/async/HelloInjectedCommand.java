package org.javalite.async;

import com.google.inject.Inject;
import org.javalite.async.services.GreetingService;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class HelloInjectedCommand implements Command {

    @Inject
    private GreetingService greetingService;

    public static String result;

    private String message;

    public HelloInjectedCommand(String message) {
        this.message = message;
    }

    public HelloInjectedCommand() {} // have to have a default constructor

    @Override
    public void fromString(String commandString) {
        this.message = commandString;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public void execute() {
        result = message + greetingService.getGreeting();
    }
}