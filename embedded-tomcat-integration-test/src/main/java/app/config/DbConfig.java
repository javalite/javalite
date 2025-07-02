package app.config;


import org.javalite.activeweb.AbstractDBConfig;
import org.javalite.activeweb.AppContext;

import static org.javalite.app_config.AppConfig.p;

public class DbConfig extends AbstractDBConfig {

    @Override
    public void init(AppContext appContext) {
        environment("development")
                .testing()
                .jdbc(  "org.mariadb.jdbc.Driver", "jdbc:mariadb://localhost:3309/javalite_db",
                        "root","p@ssw0rd");
        environment("development").jndi("java:comp/env/" + p("embedded.tomcat.pool.name"));
    }
}
