package app.services;

import com.google.inject.AbstractModule;

public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HelloService.class).asEagerSingleton();
    }
}
