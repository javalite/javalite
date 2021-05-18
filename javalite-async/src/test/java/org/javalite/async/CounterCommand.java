package org.javalite.async;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Eric Scott
 */
public class CounterCommand extends Command {

    private AtomicInteger counter;

    public CounterCommand(AtomicInteger counter) {
        this.counter = counter;
    }

    @Override
    public void execute() {
        counter.incrementAndGet();
    }

}