package org.javalite.activejdbc.connection_config;

import org.javalite.app_config.AppConfig;
import org.junit.Test;

import static org.javalite.test.jspec.JSpec.the;

public class DBConfigurationSpec {
    @Test
    public void shouldLoadUniqueConnections(){

//        DBConfiguration.loadConfiguration("/database-test.properties");
//        the(DBConfiguration.getConnectionConfigsForCurrentEnv().size()).shouldBeEqual(2);
//        the(DBConfiguration.getConnectionConfigsForCurrentEnv().get(0)).shouldBeA(ConnectionJdbcConfig.class);
//        the(DBConfiguration.getConnectionConfigsForCurrentEnv().get(1)).shouldBeA(ConnectionJdbcConfig.class);

        AppConfig.setActiveEnv("production");
        DBConfiguration.resetConnectionConfigs();
        DBConfiguration.loadConfiguration("/database-test.properties");
        the(DBConfiguration.getConnectionConfigsForCurrentEnv().size()).shouldEqual(1);
        AppConfig.setActiveEnv("development");
    }
}
