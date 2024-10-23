package app.config;


import org.javalite.activeweb.AbstractDBConfig;
import org.javalite.activeweb.AppContext;

import static org.javalite.app_config.AppConfig.p;

public class DbConfig extends AbstractDBConfig {

    @Override
    public void init(AppContext appContext) {
        environment("development").jndi("java:comp/env/" + p("embedded.tomcat.pool.name"));
    }
}
