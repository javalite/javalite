package org.javalite.async;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Igor Polevoy on 4/20/2021.
 */
public class BookCommand extends Command {

    private static AtomicInteger counter = new AtomicInteger(0);
    private Book book;

    public BookCommand(){}

    public BookCommand(Book book) {
        this.book = book;
    }


    public static void reset() {
        counter = new AtomicInteger(0);
    }

    public static int counter() {
        return counter.get();
    }

    @Override
    public void execute() {
        System.out.println("Executing " + getClass());
        synchronized (counter){
            counter.incrementAndGet();
        }
    }
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}