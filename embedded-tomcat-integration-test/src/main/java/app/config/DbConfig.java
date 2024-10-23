package app.config;


import org.javalite.activeweb.AbstractDBConfig;
import org.javalite.activeweb.AppContext;

public class DbConfig extends AbstractDBConfig {

    @Override
    public void init(AppContext appContext) {
        environment("development").jndi("java:comp/env/jdbc/myDatabasePool");
//        environment("development", true).jndi("java:comp/env/jdbc/myDataSource"); -- working

    }
}
