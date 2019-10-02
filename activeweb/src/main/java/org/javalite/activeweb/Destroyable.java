package org.javalite.activeweb;

/**
 * Indicates a service in Guice can be destroyed when the application shuts down.
 * Implement by service classes that are injected into the Guice container.
 *
 * @author Igor Polevoy
 */
public interface Destroyable {

    /**
     * Will be called when your app is shut down gracefully.
     */
    void destroy();
}
