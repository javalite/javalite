package org.javalite.async;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class HelloCommand extends Command {

    private static Integer counter = 0;
    private String message;


    public HelloCommand(String message) {
        this.message = message;
    }

    public HelloCommand() {} //necessary to provide

    public static void reset() {
        counter = 0;
    }

    public static int counter() {
        return counter;
    }

    @Override
    public void execute() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {}

        synchronized (counter){
            counter++;
        }
    }

    public String getMessage() {
        return message;
    }
}