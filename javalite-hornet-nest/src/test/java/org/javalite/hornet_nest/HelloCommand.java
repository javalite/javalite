package org.javalite.hornet_nest;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class HelloCommand implements Command {

    private static Integer counter = 0;

    public static int counter() {
        return counter;
    }

    private String message;

    public HelloCommand(String message) {
        this.message = message;
    }

    public HelloCommand() {} //necessary to provide

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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        synchronized (counter){
            counter++;
        }
    }
}