package app.services;


import org.javalite.activeweb.Destroyable;

public class HelloService implements Destroyable {

    @Override
    public void destroy() {
        System.err.println("HelloService destroyed!");
    }
}
