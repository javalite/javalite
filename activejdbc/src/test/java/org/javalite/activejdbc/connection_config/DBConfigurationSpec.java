package org.javalite.activejdbc.connection_config;

import org.junit.Test;

import static org.javalite.test.jspec.JSpec.the;

public class DBConfigurationSpec {
    @Test
    public void shouldLoadUniqueConnections(){
        DBConfiguration.resetConnectionConfigs();
        DBConfiguration.loadConfiguration("/database-test.properties");
        DBConfiguration.loadConfiguration("/database-test.properties");
        the(DBConfiguration.getConnectionConfigsForCurrentEnv().size()).shouldBeEqual(2);
        the(DBConfiguration.getConnectionConfigsForCurrentEnv().get(0)).shouldBeA(ConnectionJdbcConfig.class);

        the(DBConfiguration.getConnectionConfigs("production").size()).shouldEqual(1);
    }
}
