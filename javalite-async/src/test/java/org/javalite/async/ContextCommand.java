package org.javalite.async;

import org.javalite.logging.Context;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Igor Polevoy on 4/5/15.
 */
public class ContextCommand extends Command {

    private boolean context;


    public ContextCommand(boolean context) {
        this.context= context;
    }

    public ContextCommand() {} //necessary to provide

    @Override
    public void execute() {
        if(context){
            Context.put("age", "35");
        }
    }
}