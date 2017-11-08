package org.javalite.async.pooltest;

import org.javalite.async.Command;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class HelloPerformanceCommand extends Command {

    private static AtomicInteger counter = new AtomicInteger(0);
    private String message;

    public static long START;

    public HelloPerformanceCommand(String message) {
        this.message = message;
        addParams("message", message);
    }

    public HelloPerformanceCommand() {
    } //necessary to provide

    public static void reset() {
        counter = new AtomicInteger(0);
    }

    public static int counter() {
        return counter.get();
    }

    @Override
    public void execute() {
        counter.incrementAndGet();
        System.out.println(counter);
        if (counter.get() == TestSend.MESSAGES_PER_THREAD * TestSend.SENDING_THREAD_COUNT) {
            System.out.println("completed, took " + (System.currentTimeMillis() - START) + " milliseconds");
        }
    }

    public String getMessage() {
        return message;
    }
}