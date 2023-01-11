package org.javalite.async;

public class Hello2Command extends Command{

    private String greeting;

    public Hello2Command(String greeting) {
        this.greeting = greeting;
    }

    @Override
    public void execute() {
        System.out.println(greeting);
    }
}
