package org.javalite.activeweb;

import org.javalite.activejdbc.connection_config.ConnectionConfig;
import org.javalite.activejdbc.connection_config.ConnectionJdbcConfig;
import org.javalite.activejdbc.connection_config.ConnectionJndiConfig;

import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 12/1/14.
 */
public class AbstractConnectionBuilderSpec  {

    @Before
    public void before(){
        DBConfiguration.resetConnectionConfigs();
    }

    @Test
    public void shouldConfigureJDBC(){
        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                environment("development").testing().jdbc("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test123", "root", "****");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        ConnectionJdbcConfig connectionConfig = (ConnectionJdbcConfig) DBConfiguration.getConnectionConfigs().get(0);

        a(connectionConfig.getDbName()).shouldBeEqual("default");
        a(connectionConfig.isTesting()).shouldBeTrue();
        a(connectionConfig.getEnvironment()).shouldBeEqual("development");



        a(connectionConfig.getDriver()).shouldBeEqual("com.mysql.jdbc.Driver");
        a(connectionConfig.getUrl()).shouldBeEqual("jdbc:mysql://localhost/test123");
        a(connectionConfig.getUser()).shouldBeEqual("root");
        a(connectionConfig.getPassword()).shouldBeEqual("****");
    }

    @Test
    public void shouldConfigureJNDI(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                environment("prod").db("second").jndi("jdbc/123_dev");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        ConnectionJndiConfig jndiConfig= (ConnectionJndiConfig) DBConfiguration.getConnectionConfigs("prod").get(0);

        a(jndiConfig.getDbName()).shouldBeEqual("second");
        a(jndiConfig.isTesting()).shouldBeFalse();
        a(jndiConfig.getEnvironment()).shouldBeEqual("prod");

        a(jndiConfig.getDataSourceJndiName()).shouldBeEqual("jdbc/123_dev");
    }

    @Test
    public void shouldConfigureJndiFromFile(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                configFile("/database.properties");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        ConnectionJndiConfig jndiConfig = (ConnectionJndiConfig) DBConfiguration.getConnectionConfigs("production").get(0);

        a(jndiConfig.getDbName()).shouldBeEqual("default");
        a(jndiConfig.isTesting()).shouldBeFalse();
        a(jndiConfig.getEnvironment()).shouldBeEqual("production");

        a(jndiConfig.getDataSourceJndiName()).shouldBeEqual("java:comp/env/jdbc/prod");
    }

    @Test
    public void shouldConfigureJdbcFromFile(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                configFile("/database.properties");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        //test first connection spec
        ConnectionJdbcConfig jdbcConfig = (ConnectionJdbcConfig) DBConfiguration.getConnectionConfigs("development").get(0);
        a(jdbcConfig.getDbName()).shouldBeEqual("default");
        a(jdbcConfig.getEnvironment()).shouldBeEqual("development");
        a(jdbcConfig.isTesting()).shouldBeFalse();


        a(jdbcConfig.getDriver()).shouldBeEqual("com.mysql.jdbc.Driver");
        a(jdbcConfig.getUrl()).shouldBeEqual("jdbc:mysql://localhost/proj_dev");
        a(jdbcConfig.getUser()).shouldBeEqual("john");
        a(jdbcConfig.getPassword()).shouldBeEqual("pwd");

        //test second connection spec
        jdbcConfig = (ConnectionJdbcConfig) DBConfiguration.getConnectionConfigs("development").get(1);
        a(jdbcConfig.getDbName()).shouldBeEqual("default");
        a(jdbcConfig.getEnvironment()).shouldBeEqual("development");
        a(jdbcConfig.isTesting()).shouldBeTrue();


        a(jdbcConfig.getDriver()).shouldBeEqual("com.mysql.jdbc.Driver");
        a(jdbcConfig.getUrl()).shouldBeEqual("jdbc:mysql://localhost/test");
        a(jdbcConfig.getUser()).shouldBeEqual("mary");
        a(jdbcConfig.getPassword()).shouldBeEqual("pwd1");
    }



    @Test
    public void shouldOverrideConnectionSpecForTheSameEnvironment(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                configFile("/database1.properties");
                environment("production", true).jndi("java:comp/env/jdbc/prod_new");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        List<ConnectionConfig> wrappers = DBConfiguration.getConnectionConfigs("production");

        //we configured two for production, one in file, one in class. But the class config overrides one in file.
        the(wrappers.size()).shouldBeEqual(1);

        ConnectionJndiConfig connectionSpec = (ConnectionJndiConfig)  wrappers.get(0);
        the(connectionSpec.getDataSourceJndiName()).shouldBeEqual("java:comp/env/jdbc/prod_new");
    }


    /**
     * This feature is needed because often times, you have different configuration locally vs another
     * environment where you need to run tests. It is possible to achieve with Maven profiles, but kind of hacky.
     * Example: development env requires to connect to localhost. Jenkins when running tests, requires to connect to jenkins_db host.
     * This feature is not specific to Jenkins :)
     *
     * The new solution is clean.
     */
    @Test
    public void should_configure_different_test_configs_for_development_and_jenkins(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                configFile("/database2.properties");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        List<ConnectionConfig> connectionConfigs = DBConfiguration.getConnectionConfigs("development");

        the(connectionConfigs.size()).shouldBeEqual(2);


        ConnectionConfig dev = null;
        ConnectionConfig test = null;

        //have to do this because the order of specs is not deterministic
        for (ConnectionConfig connectionConfig : connectionConfigs) {
            if(connectionConfig.isTesting()){
                test = connectionConfig;
            }else {
                dev = connectionConfig;
            }
        }
        the(((ConnectionJdbcConfig)test).getUrl()).shouldBeEqual("jdbc:mysql://localhost/test");
        the(((ConnectionJdbcConfig)dev).getUrl()).shouldBeEqual("jdbc:mysql://localhost/dev");
    }
}