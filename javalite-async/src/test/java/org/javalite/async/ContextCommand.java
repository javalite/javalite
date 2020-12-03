package org.javalite.async;

import org.javalite.logging.Context;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class ContextCommand extends Command {

    private boolean context;
    private static AtomicInteger counter = new AtomicInteger(0);


    public ContextCommand(boolean context) {
        this.context= context;
    }
    public static void reset() {
        counter = new AtomicInteger(0);
    }

    public ContextCommand() {} //necessary to provide

    @Override
    public void execute() {
        synchronized (counter){
            counter.incrementAndGet();
        }

        if(context){
            Context.put("weight", "35lb");
        }
    }

    public static int counter() {
        return counter.get();
    }
}