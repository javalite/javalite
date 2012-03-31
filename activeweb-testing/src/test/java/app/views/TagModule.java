package app.views;

import com.google.inject.AbstractModule;

/**
 * @author Igor Polevoy: 3/30/12 5:12 PM
 */
public class TagModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GreetingService.class).asEagerSingleton();
    }
}
