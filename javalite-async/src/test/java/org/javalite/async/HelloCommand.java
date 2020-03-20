package org.javalite.async;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class HelloCommand extends Command {

    private static AtomicInteger counter = new AtomicInteger(0);
    private String message;


    public HelloCommand(String message) {
        this.message = message;
    }

    public HelloCommand() {} //necessary to provide

    public static void reset() {
        counter = new AtomicInteger(0);
    }

    public static int counter() {
        return counter.get();
    }

    @Override
    public void execute() {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ignore) {}

        synchronized (counter){
            counter.incrementAndGet();
        }
    }

    public String getMessage() {
        return message;
    }
}